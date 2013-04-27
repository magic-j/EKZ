package com.magic_j.ekz;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.magic_j.pluginhelpers.RegionHelper;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


public class Ekz extends JavaPlugin implements Listener{

	private EkzRegister ekzRegister;	
		
	@Override
    public void onEnable() {
		Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    if (wgPlugin != null || (wgPlugin instanceof WorldGuardPlugin)) {		        
	    	 RegionHelper.init((WorldGuardPlugin) wgPlugin);
	    }
		
		ekzRegister = new EkzRegister();	
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		pm.registerEvents(new PlayerEvents(this), this);
		
		getCommand("ekz").setExecutor(new EkzCommands(this));
    }
 
    @Override
    public void onDisable() {
    	getEkzRegister().close();
    }

	public EkzRegister getEkzRegister() {
		return ekzRegister;
	}
}
