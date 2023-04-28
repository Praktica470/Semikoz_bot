package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.example.URLToImage.IMGConverter;

public class PlayerManager {
    private static final String TIME_FORMAT = "H' hrs. 'mm' min. 'ss' sec.'";
    private static final String TIME_FORMAT_WITHOUT_HOURS = "m' min. 'ss' sec.'";
    private static PlayerManager INSTANCE;
    private static ArrayList<String> HISTORY = new ArrayList<String>();
    private static ArrayList<String> HISTORY_URL = new ArrayList<String>();
    private static ArrayList<String> HISTORY_ID = new ArrayList<String>();
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(Guild channel, String trackURL, MessageChannel channel2) {
        final GuildMusicManager musicManager = this.getMusicManager(channel);
        this.audioPlayerManager.loadItem(trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(track.getInfo().title, trackURL);
            eb.setColor(new Color(0x387B6E));
            if(!DateFormatUtils.formatUTC(track.getDuration(), TIME_FORMAT).startsWith("0")) {
                eb.setDescription(DateFormatUtils.formatUTC(track.getDuration(), TIME_FORMAT));
            }
            else {
                eb.setDescription(DateFormatUtils.formatUTC(track.getDuration(), TIME_FORMAT_WITHOUT_HOURS));
            }
            eb.setAuthor(track.getInfo().author);
            eb.setImage(IMGConverter(trackURL));
            channel2.sendMessageEmbeds(eb.build()).queue(message -> {
                HISTORY_ID.add(message.getId());
            });
                if(HISTORY.size() >= 30){
                    HISTORY.clear();
                    HISTORY_URL.clear();
                }
                HISTORY.add(track.getInfo().title + " — "
                        + track.getInfo().author);
                HISTORY_URL.add(trackURL);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                int i = 0;
                long durration = 0;
                for (AudioTrack track : playlist.getTracks()) {
                    i++;
                    musicManager.scheduler.queue(track);
                    durration += track.getDuration();
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(playlist.getName(), trackURL);
                eb.setColor(new Color(0x387B6E));
                if(!DateFormatUtils.formatUTC(durration, TIME_FORMAT).startsWith("0")) {
                    eb.setDescription(DateFormatUtils.formatUTC(durration, TIME_FORMAT));
                }
                else {
                    eb.setDescription(DateFormatUtils.formatUTC(durration, TIME_FORMAT_WITHOUT_HOURS));
                }
                eb.setAuthor(playlist.getSelectedTrack().getInfo().author);
                eb.setImage(IMGConverter(trackURL));
                channel2.sendMessageEmbeds(eb.build()).queue(message -> {
                    HISTORY_ID.add(message.getId());
                });
                if(HISTORY.size() >= 30){
                    HISTORY.clear();
                    HISTORY_URL.clear();
                }
                HISTORY.add("Playlist: " + playlist.getName() + " — " + playlist.getSelectedTrack().getInfo().author);
                HISTORY_URL.add(trackURL);
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public static PlayerManager getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public static ArrayList getHISTORY() {
        if(HISTORY == null){
            HISTORY = new ArrayList<String>();
        }
        return HISTORY;
    }
    public static ArrayList getHISTORY_URL(){
        if(HISTORY_URL == null){
            HISTORY_URL = new ArrayList<String>();
        }
        return HISTORY_URL;
    }
    public static ArrayList getHISTORY_ID(){
        if(HISTORY_ID == null){
            HISTORY_ID = new ArrayList<String>();
        }
        return HISTORY_ID;
    }

    public static String getTIME_FORMAT(){
        return TIME_FORMAT;
    }
    public static String getTIME_FORMAT_WITHOUT_HOURS(){
        return TIME_FORMAT_WITHOUT_HOURS;
    }
}