package upc.edu.cep.flume.selectors;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.channel.AbstractChannelSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DCEPFilterSelector extends AbstractChannelSelector {

    public static final String CONFIG_CHANNELS = "channels";

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory
            .getLogger(DCEPFilterSelector.class);

    private static final List<Channel> EMPTY_LIST =
            Collections.emptyList();

    private String channelNames;

    private Map<String, Channel> channels;


    @Override
    public List<Channel> getRequiredChannels(Event event) {

        String[] rules = event.getHeaders().get("Rules").trim().split(";");

        Set<Channel> channelsSet = new HashSet<>();

        for (String r : rules) {
            r = r.trim();
            if (!r.equals("")) {
                if (channels.get(r)!=null)
                    channelsSet.add(channels.get(r));
                //System.out.println("+++++++++++++++++++++++++++++++++++  " + channels.get(r));
            }
        }
        //System.out.println("---------------------------------------------  " + event.getHeaders().get("Rules") + "++" + channelsSet.size());

        List<Channel> aaa = new ArrayList<>(channelsSet);

        //System.out.println("---------------------------------------------  " + event.getHeaders().get("Rules") + "++" + aaa.size());


        return aaa;
    }

    @Override
    public List<Channel> getOptionalChannels(Event event) {
        return EMPTY_LIST;
    }

    @Override
    public void configure(Context context) {
        this.channelNames = context.getString(CONFIG_CHANNELS);
        try {
            Files.write(Paths.get("/home/osboxes/upc-cep/cep1.txt"), ("channels: " + this.channelNames + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.channels = new HashMap<>();

        Map<String, Channel> channelNameMap = new HashMap<String, Channel>();
        for (Channel ch : getAllChannels()) {
            channelNameMap.put(ch.getName(), ch);
        }

        if (channelNameMap.size() > 0) {

            List<Channel> configuredChannels = getChannelListFromNames(
                    channelNames,
                    channelNameMap);

            for (Channel channel : configuredChannels) {
                String[] rules = context.getString(channel.getName() + "." + DCEPSelectorConstants.RULES).trim().split(";");
                for (String r : rules) {
                    r = r.trim();
                    if (!r.equals("")) {
                        channels.put(r, channel);
                        try {
                            Files.write(Paths.get("/home/osboxes/upc-cep/cep1.txt"), ("channel: " + channel.getName() + " -> " + r + "\n").getBytes(), StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
