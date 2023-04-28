package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Arrays;
import java.util.List;
import java.awt.Color;
import java.util.concurrent.TimeUnit;

import static org.example.PlayerManager.getHISTORY_ID;
import static org.example.PlayerManager.getTIME_FORMAT_WITHOUT_HOURS;
import static org.example.URLToImage.IMGConverter;

public class SendCommand extends ListenerAdapter
{
    final PlayerManager playerManager = new PlayerManager();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "hello")) {
            final String content = event.getMessage().getContentRaw();
            final List<String> command = Arrays.asList(content.split(" "));
            Guild guild = event.getGuild();
            VoiceChannel channel = guild.getVoiceChannelsByName(command.get(1), true).get(0);
            AudioManager audioManager = guild.getAudioManager();
            audioManager.openAudioConnection(channel);
            getHISTORY_ID().add(event.getMessage().getId());
        }
        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "play")) {
            final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
            final AudioPlayer audioPlayer = musicManager.audioPlayer;
            Guild guild = event.getGuild();
            MessageChannel channel2 = event.getChannel();
            final String content = event.getMessage().getContentRaw();
            final List<String> command = Arrays.asList(content.split(" "));
            PlayerManager.getINSTANCE()
                    .loadAndPlay(guild, command.get(1), channel2);
            getHISTORY_ID().add(event.getMessage().getId());
        }
        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "stop")) {
            final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
            musicManager.scheduler.player.stopTrack();
            musicManager.scheduler.queue.clear();
            MessageChannel channel = event.getChannel();
            getHISTORY_ID().add(event.getMessage().getId());
            channel.sendMessage("Track stopped.").queue(message -> {
                message.delete().queueAfter(5, TimeUnit.SECONDS);
            });
            int s = PlayerManager.getHISTORY_ID().size();
            for (int i = 0; i < s; i++) {
                channel.deleteMessageById(getHISTORY_ID().get(i).toString()).queue();
            }
            getHISTORY_ID().clear();
        }
        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "skip")) {
            final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
            final AudioPlayer audioPlayer = musicManager.audioPlayer;
            MessageChannel channel = event.getChannel();
            getHISTORY_ID().add(event.getMessage().getId());
            if(audioPlayer.getPlayingTrack() == null){
                channel.sendMessage("No playing tracks.").queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
            }
            else {
                channel.sendMessage("Track skipped.").queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
                musicManager.scheduler.nextTrack();
            }
        }
        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "current")) {
            final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
            final AudioPlayer audioPlayer = musicManager.audioPlayer;
            MessageChannel channel = event.getChannel();
            getHISTORY_ID().add(event.getMessage().getId());
            if (audioPlayer.getPlayingTrack() == null) {
                channel.sendMessage("No playing tracks.").queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
            }
            else {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(audioPlayer.getPlayingTrack().getInfo().title, audioPlayer.getPlayingTrack().getInfo().uri);
                eb.setColor(new Color(0x387B6E));
                if(!DateFormatUtils.formatUTC(audioPlayer.getPlayingTrack().getDuration(), PlayerManager.getTIME_FORMAT()).startsWith("0")) {
                    eb.setDescription(DateFormatUtils.formatUTC(audioPlayer.getPlayingTrack().getDuration(), PlayerManager.getTIME_FORMAT()));
                }
                else {
                    eb.setDescription(DateFormatUtils.formatUTC(audioPlayer.getPlayingTrack().getDuration(), getTIME_FORMAT_WITHOUT_HOURS()));
                }
                eb.setAuthor(audioPlayer.getPlayingTrack().getInfo().author);
                eb.setImage(IMGConverter(audioPlayer.getPlayingTrack().getInfo().uri));
                channel.sendMessageEmbeds(eb.build()).queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
//                playerManager.getHISTORY_ID().add(channel.getLatestMessageId());
            }

        }
        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "pause")) {
            final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
            musicManager.scheduler.player.setPaused(true);
            MessageChannel channel = event.getChannel();
            getHISTORY_ID().add(event.getMessage().getId());
            channel.sendMessage("Track paused.").queue(message -> {
                getHISTORY_ID().add(message.getId());
            });
        }
        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "start")) {
            final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
            MessageChannel channel = event.getChannel();
            getHISTORY_ID().add(event.getMessage().getId());
            if(musicManager.scheduler.player.isPaused())
            {
                musicManager.scheduler.player.setPaused(false);
                channel.sendMessage("StartTrack.").queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
            }
            else {
                channel.sendMessage("No playing tracks.").queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
            }
        }

        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "history")) {
            MessageChannel channel = event.getChannel();
            StringBuilder stringBuilder = new StringBuilder();
            getHISTORY_ID().add(event.getMessage().getId());
            try {
                int s = PlayerManager.getHISTORY().size();
                for (int i = 0; i < s; i++) {
                    stringBuilder.append((i + 1) + ". " + playerManager.getHISTORY().get(i) + "\n");
                }
                channel.sendMessage(stringBuilder).queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
            }
            catch (Exception exception){
                channel.sendMessage(exception.getMessage()).queue(message -> {
                    getHISTORY_ID().add(message.getId());
                });
            }
        }

        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "hist_pl")) {
            MessageChannel channel = event.getChannel();
            Guild guild = event.getGuild();
            StringBuilder stringBuilder1 = new StringBuilder();
            getHISTORY_ID().add(event.getMessage().getId());
            try {
                final String content = event.getMessage().getContentRaw();
                final List<String> command = Arrays.asList(content.split(" "));
                int s = Integer.parseInt(command.get(1)) - 1;
                stringBuilder1.append(playerManager.getHISTORY_URL().get(s));
                playerManager.getINSTANCE()
                        .loadAndPlay(guild, stringBuilder1.toString(), channel);
            }
            catch (Exception exception){
                channel.sendMessage(exception.getMessage());
            }
        }

        if(event.getMessage().getContentRaw().startsWith(Config.PREFIX + "help")) {
            MessageChannel channel = event.getChannel();
            getHISTORY_ID().add(event.getMessage().getId());
            channel.sendMessage("Commands: \n ———————————————————— \n"
                            + "**!hello <Voice channel name>** - connecting to the specified voice channel\n"
                            + "**!play <URL>** - plays the track in the URL\n"
                            + "**!stop** - stops the music\n"
                            + "**!skip** - skips the current track\n"
                            + "**!current** - shows the current track\n"
                            + "**!pause** - pauses the current track\n"
                            + "**!start** - starts the current track\n"
                            + "**!history** - shows the track's history\n"
                            + "**!hist_pl <track's number in your history>** - plays the specified track\n")
                    .queue(message -> {
                        getHISTORY_ID().add(message.getId());
                    });
        }
    }
}
