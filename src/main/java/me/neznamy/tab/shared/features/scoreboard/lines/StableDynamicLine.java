package me.neznamy.tab.shared.features.scoreboard.lines;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.scoreboard.Scoreboard;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public abstract class StableDynamicLine extends ScoreboardLine {

	protected Scoreboard parent;
	protected int lineNumber;
	protected String text;

	public StableDynamicLine(Scoreboard parent, int lineNumber, String text) {
		super(lineNumber);
		this.parent = parent;
		this.lineNumber = lineNumber;
		this.text = text;
		refreshUsedPlaceholders();
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(text);
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!parent.players.contains(refreshed)) return; //player has different scoreboard displayed
		List<String> prefixsuffix = replaceText(refreshed, force, false);
		if (prefixsuffix == null) return;
		refreshed.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefixsuffix.get(0), prefixsuffix.get(1), "always", "always", 69), TabFeature.SCOREBOARD);
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(teamName, text);
		List<String> prefixsuffix = replaceText(p, true, true);
		if (prefixsuffix == null) return;
		addLine(p, teamName, getPlayerName(), prefixsuffix.get(0), prefixsuffix.get(1), getScoreFor(p));
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.players.contains(p) && p.getProperty(teamName).get().length() > 0) {
			removeLine(p, getPlayerName(), teamName);
		}
	}

	protected List<String> replaceText(TabPlayer p, boolean force, boolean suppressToggle) {
		Property scoreproperty = p.getProperty(teamName);
		boolean emptyBefore = scoreproperty.get().length() == 0;
		if (!scoreproperty.update() && !force) return null;
		String replaced = scoreproperty.get();
		if (p.getVersion().getMinorVersion() < 16) {
			replaced = IChatBaseComponent.fromColoredText(replaced).toLegacyText(); //converting RGB to legacy here to avoid splitting in the middle of RGB code
		}
		String[] split = split(p, replaced);
		String prefix = split[0];
		String suffix = split[1];
		if (replaced.length() > 0) {
			if (emptyBefore) {
				//was "", now it is not
				addLine(p, teamName, getPlayerName(), prefix, suffix, getScoreFor(p));
				return null;
			} else {
				return Arrays.asList(prefix, suffix);
			}
		} else {
			if (!suppressToggle) {
				//new string is "", but before it was not
				removeLine(p, getPlayerName(), teamName);
			}
			return null;
		}
	}
	
	private String[] split(TabPlayer p, String text) {
		//ProtocolSupport limiting length to 14 for <1.13 on 1.13+ server
		int charLimit = TAB.getInstance().getPlatform().getSeparatorType().equals("world") && 
			ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13 && 
			p.getVersion().getMinorVersion() < 13 ? 14 : 16;
		String prefix;
		String suffix;
		if (text.length() > charLimit && p.getVersion().getMinorVersion() < 13) {
			prefix = text.substring(0, charLimit);
			suffix = text.substring(charLimit, text.length());
			if (prefix.charAt(charLimit-1) == '\u00a7') {
				prefix = prefix.substring(0, charLimit-1);
				suffix = '\u00a7' + suffix;
			}
			String last = TAB.getInstance().getPlaceholderManager().getLastColors(IChatBaseComponent.fromColoredText(prefix).toLegacyText());
			suffix = last + suffix;
		} else {
			prefix = text;
			suffix = "";
		}
		return new String[] {prefix, suffix};
	}

	public abstract int getScoreFor(TabPlayer p);
}