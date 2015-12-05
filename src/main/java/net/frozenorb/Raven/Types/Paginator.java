package net.frozenorb.Raven.Types;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 3/10/2015
 * <p/>
 *
 * @author Ryan
 */
public abstract class Paginator<T> {

    String HEADER = "------[%d/%d]------";

    int per_page;
    int page_number;

    int max_pages;

    List<String> page;

    public Paginator(List<T> original, int page, int per_page) {
        this.per_page = per_page;
        this.page_number = page;
        this.max_pages = (int) Math.ceil(original.size() / per_page) + 1;

        if (original.size() % this.per_page == 0) {
            max_pages--;
        }



        this.page = toPage(original);
    }


    public void setHeader(String header) {
        this.HEADER = ChatColor.translateAlternateColorCodes('&', header);
    }

    public List<String> toPage(List<T> original) {
        List<String> toReturn = new ArrayList<>();

        if (page_number <= 0) {
            page_number = 1;
        }

        for (int x = per_page * (page_number - 1); x < (page_number * per_page) && x < original.size(); x++) {
            toReturn.add(format(original.get(x), x));
        }
        return toReturn;
    }

    public void print(CommandSender player) {
        player.sendMessage(String.format(HEADER, page_number, max_pages));
        for (String s : page) {
            player.sendMessage(s);
        }
    }

    public abstract String format(T entry, int index);
}
