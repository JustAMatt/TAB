package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Velocity registry to register velocity-only placeholders
 */
public class VelocityPlaceholderRegistry implements PlaceholderRegistry {

	//instance of proxyserver
	private ProxyServer server;
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of proxyserver
	 */
	public VelocityPlaceholderRegistry(ProxyServer server) {
		this.server = server;
	}
	
	@Override
	public List<Placeholder> registerPlaceholders() {
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		for (RegisteredServer rServer : server.getAllServers()) {
			placeholders.add(new ServerPlaceholder("%online_" + rServer.getServerInfo().getName() + "%", 1000) {
				public String get() {
					return rServer.getPlayersConnected().size()+"";
				}
			});
		}
		return placeholders;
	}
}