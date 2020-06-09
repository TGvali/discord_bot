/*
 * Copyright 2018 John Grosh (jagrosh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

/**
 * @author John Grosh (jagrosh)
 */
public class BotConfig
{
    private final Prompt prompt;

    private static final String CONTEXT = "Config";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages", Locale.ENGLISH);

    private Path path;

    private String token;
    private String prefix;
    private String altPrefix;
    private String helpWord;
    private String playlistsFolder;
    private String successEmoji;
    private String warningEmoji;
    private String errorEmoji;
    private String loadingEmoji;
    private String searchingEmoji;
    private boolean stayInChannel;
    private boolean songInGame;
    private boolean npImages;
    private boolean updateAlerts;
    private boolean useEval;
    private boolean dbots;
    private long owner;
    private long maxSeconds;
    private OnlineStatus status;
    private Game game;
    private Config aliases;

    private boolean valid = false;

    public BotConfig(Prompt prompt)
    {
        this.prompt = prompt;
    }

    public void load()
    {
        valid = false;

        try
        {
            path = OtherUtil.getPath(System.getProperty("config.file", System.getProperty("config", "config.txt")));
            if (path.toFile().exists())
            {
                if (System.getProperty("config.file") == null)
                {
                    System.setProperty("config.file", System.getProperty("config", "config.txt"));
                }
                ConfigFactory.invalidateCaches();
            }

            Config config = ConfigFactory.load();

            token = config.getString("token");
            prefix = config.getString("prefix");
            altPrefix = config.getString("altprefix");
            helpWord = config.getString("help");
            owner = config.getLong("owner");
            successEmoji = config.getString("success");
            warningEmoji = config.getString("warning");
            errorEmoji = config.getString("error");
            loadingEmoji = config.getString("loading");
            searchingEmoji = config.getString("searching");
            game = OtherUtil.parseGame(config.getString("game"));
            status = OtherUtil.parseStatus(config.getString("status"));
            stayInChannel = config.getBoolean("stayinchannel");
            songInGame = config.getBoolean("songinstatus");
            npImages = config.getBoolean("npimages");
            updateAlerts = config.getBoolean("updatealerts");
            useEval = config.getBoolean("eval");
            maxSeconds = config.getLong("maxtime");
            playlistsFolder = config.getString("playlistsfolder");
            aliases = config.getConfig("aliases");
            dbots = owner == 113156185389092864L;

            boolean write = false;

            if (token == null || token.isEmpty() || token.equalsIgnoreCase("BOT_TOKEN_HERE"))
            {
                token = prompt.prompt(MESSAGES.getString("bot_token_prompt"));
                if (token == null)
                {
                    prompt.alert(Prompt.Level.WARNING, CONTEXT, "No token provided! Exiting.\n\nConfig Location: " + path.toAbsolutePath().toString());
                    return;
                }
                else
                {
                    write = true;
                }
            }

            if (owner <= 0)
            {
                try
                {
                    owner = Long.parseLong(prompt.prompt(MESSAGES.getString("owner_id_prompt")));
                }
                catch (NumberFormatException | NullPointerException ex)
                {
                    owner = 0;
                }
                if (owner <= 0)
                {
                    prompt.alert(Prompt.Level.ERROR, CONTEXT, "Invalid User ID! Exiting.\n\nConfig Location: " + path.toAbsolutePath().toString());
                    System.exit(0);
                }
                else
                {
                    write = true;
                }
            }

            if (write)
            {
                dumpToFile();
            }

            valid = true;
        }
        catch (ConfigException ex)
        {
            prompt.alert(Prompt.Level.ERROR, CONTEXT, ex + ": " + ex.getMessage() + "\n\nConfig Location: " + path.toAbsolutePath().toString());
        }
    }

    public void dumpToFile()
    {
        String original = OtherUtil.loadResource(this, "/reference.conf");
        byte[] bytes;

        if (original == null)
        {
            bytes = String.format("token = %s%nowner= %s", token, owner).getBytes();
        }
        else
        {
            bytes = original.replace("BOT_TOKEN_HERE", token).replace("0 // OWNER ID", Long.toString(owner)).getBytes();
        }

        try
        {
            Files.write(path, bytes);
        }
        catch (IOException ex)
        {
            prompt.alert(Prompt.Level.WARNING, CONTEXT,
                    String.format(MESSAGES.getString("config_dump_failure"), ex, path.toAbsolutePath()));
        }
    }

    public boolean isValid()
    {
        return valid;
    }

    public String getConfigLocation()
    {
        return path.toFile().getAbsolutePath();
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getAltPrefix()
    {
        return "NONE".equalsIgnoreCase(altPrefix) ? null : altPrefix;
    }

    public String getToken()
    {
        return token;
    }

    public long getOwnerId()
    {
        return owner;
    }

    public String getSuccess()
    {
        return successEmoji;
    }

    public String getWarning()
    {
        return warningEmoji;
    }

    public String getError()
    {
        return errorEmoji;
    }

    public String getLoading()
    {
        return loadingEmoji;
    }

    public String getSearching()
    {
        return searchingEmoji;
    }

    public Game getGame()
    {
        return game;
    }

    public OnlineStatus getStatus()
    {
        return status;
    }

    public String getHelp()
    {
        return helpWord;
    }

    public boolean getStay()
    {
        return stayInChannel;
    }

    public boolean getSongInStatus()
    {
        return songInGame;
    }

    public String getPlaylistsFolder()
    {
        return playlistsFolder;
    }

    public boolean getDBots()
    {
        return dbots;
    }

    public boolean useUpdateAlerts()
    {
        return updateAlerts;
    }

    public boolean useEval()
    {
        return useEval;
    }

    public boolean useNPImages()
    {
        return npImages;
    }

    public long getMaxSeconds()
    {
        return maxSeconds;
    }

    public String getMaxTime()
    {
        return FormatUtil.formatTime(maxSeconds * 1000);
    }

    public boolean isTooLong(AudioTrack track)
    {
        if (maxSeconds <= 0)
        {
            return false;
        }
        return Math.round(track.getDuration() / 1000.0) > maxSeconds;
    }

    public String[] getAliases(String command)
    {
        try
        {
            return aliases.getStringList(command).toArray(new String[0]);
        }
        catch (NullPointerException | ConfigException.Missing e)
        {
            return new String[0];
        }
    }
}
