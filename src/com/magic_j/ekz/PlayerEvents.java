package com.magic_j.ekz;

import static com.Acrobot.ChestShop.Signs.ChestShopSign.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.magic_j.pluginhelpers.RegionHelper;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlayerEvents implements Listener {

	private Ekz ekz;

	public PlayerEvents(Ekz ekz) {
		this.ekz = ekz;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) {
        	return;
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.ENCHANTMENT_TABLE) {
        	Block blockDown = block.getRelative(BlockFace.DOWN);        	
        	if (blockDown != null && blockDown.getType() == Material.WALL_SIGN ) {
        		Sign sign = (Sign)blockDown.getState();
        		if (sign.getLine(0).equalsIgnoreCase("[ekz shops]")) {
        			String ekzRegion = RegionHelper.getCurrentRegionName(e.getPlayer());
        			if (!ekzRegion.isEmpty()) {
	        			e.setCancelled(true);
	        			ekz.getEkzRegister().showShops(e.getPlayer(), ekzRegion, 0);
        			}
        		}
        	}
        }
    }
	
	@EventHandler
    public void onShopCreation(ShopCreatedEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getSignLines();
		String shopOwnerName = lines[NAME_LINE];
        if (ChestShopSign.isAdminShop(shopOwnerName)) {        	
        	player.sendMessage("§6Angebot wurde für Adminshop registriert");
        }
        else {        	        	
        	double buyPrice = PriceUtil.getBuyPrice(lines[PRICE_LINE]);
    		double sellPrice = PriceUtil.getSellPrice(lines[PRICE_LINE]);
    		ItemStack item = MaterialUtil.getItem(lines[ITEM_LINE]);
    		int amount = 0;
    		try {
    			amount = Integer.parseInt(lines[QUANTITY_LINE]);
    		}
    		catch (NumberFormatException e) {
    			player.sendMessage("Anzahl '" + lines[1] + "' konnte nicht gelesen werden!");
    		}
    		if (amount < 1) {
    		    amount = 1;
    		}
    		item.setAmount(amount);
    		
    		String regStr = amount + "x " + item.getType().toString() + " ";
    		regStr += (buyPrice>0?"B:" + Double.toString(buyPrice)+" ":"");
    		regStr += (sellPrice>0?"S:" + Double.toString(sellPrice)+" ":"");
    		
    		ProtectedRegion shopRegion = RegionHelper.getBlockRegion(event.getSign().getBlock());
    		if (shopRegion == null) {
    			player.sendMessage("§6Keine Region gefunden, Angebot konnte nicht registriert werden!");
    			return;
    		}
    		
        	if (ekz.getEkzRegister().addItem(shopRegion.getId(), shopOwnerName, item, buyPrice, sellPrice)) {
        		player.sendMessage("§6Angebot wurde für Shop registriert (" + regStr + ")");
        	}
        	else {
        		player.sendMessage("§6Angebot konnte nicht registriert werden!");
        	}        	
        }
    }

	
	@EventHandler
	public void onShopDestroy(ShopDestroyedEvent event) {
		Player player = event.getDestroyer();		
		String[] lines = event.getSign().getLines();
		String shopOwnerName = lines[NAME_LINE];
		
		if (ChestShopSign.isAdminShop(shopOwnerName)) {        	
        	//player.sendMessage("§6Angebot vom Adminshop wurde gelöscht");
        }
        else { 
        	ItemStack item = MaterialUtil.getItem(lines[ITEM_LINE]);
        	
			ProtectedRegion shopRegion = RegionHelper.getBlockRegion(event.getSign().getBlock());
			if (shopRegion == null) {
    			player.sendMessage("§6Keine Region gefunden, Angebot konnte nicht registriert werden!");
    			return;
    		}			
		
			if (ekz.getEkzRegister().delItem(shopRegion.getId(), shopOwnerName, item)) {
	    		player.sendMessage("§6Angebot wurde aus Register gelöscht");
	    	}
	    	else {
	    		player.sendMessage("§6Angebot wurde nicht im Register gefunden!");
	    	} 
        }
    }

}
