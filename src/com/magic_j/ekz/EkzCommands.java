package com.magic_j.ekz;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.magic_j.pluginhelpers.RegionHelper;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class EkzCommands implements CommandExecutor {

	private Ekz ekz;
	
	public EkzCommands(Ekz ekz) {
		this.ekz = ekz;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("ekz")) {
			return true;
		}
		if (args.length < 1) {				
			return false;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("§6Die Befehle sind nur von Spielern verwendbar!");
			return false;				
		}
		
		Player currentPlayer = (Player)sender;
	
		if (args[0].equalsIgnoreCase("add")) {
			addEkzAtMyPosition(currentPlayer);
			return true;
		}		
		if (args[0].equalsIgnoreCase("delete")) {
			if (args.length < 2) {
				sender.sendMessage("§6Zu wenige Parameter: /ekz delete <ekz_region>");
				return true;
			}
			deleteEkzByRegionName(sender, args[1]);
			return true;
		}		
		if (args[0].equalsIgnoreCase("addshop")) {
			if (args.length < 3) {
				sender.sendMessage("§6Zu wenige Parameter: /ekz addshop <shop_id> <shop_region>");
				return true;
			}
			addShopWithIdInRegion(sender, args[1], args[2]);									
			return true;
		}	
		if (args[0].equalsIgnoreCase("delshop")) {
			if (args.length < 2) {
				sender.sendMessage("§6Zu wenige Parameter: /ekz delshop <shop_id>");
				return true;
			}
			deleteShopById(sender, args[1]);		
			return true;
		}	
		if (args[0].equalsIgnoreCase("list")) {
			currentPlayer.sendMessage("§6Einkaufzentren: " + ekz.getEkzRegister().getEkzNames());
			return true;
		}		
		if (args[0].equalsIgnoreCase("shops")) {
			if (args.length < 1) {
				currentPlayer.sendMessage("§6Zu wenige Parameter: /ekz shops <seite>");
				return true;
			}
			int page = 0;
			if (args.length > 2) {
				page = Integer.parseInt(args[1]);
			}
			showShopPages(currentPlayer, page);
			return true;
		}
		if (args[0].equalsIgnoreCase("shoplist")) {
			showShopList(currentPlayer);
			return true;
		}
		if (args[0].equalsIgnoreCase("additem")) {
			registerItemToShop(currentPlayer);
			return true;
		}
		if (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("buy")) {
			if (args.length < 2) {
				currentPlayer.sendMessage("§6Zu wenige Parameter: /ekz "+args[0] + " <item_name>");
				return true;
			}
			int page = 0;
			if (args.length > 2) {
				try {
					page = Integer.parseInt(args[2]);
				}
				catch (NumberFormatException e) { }
			}
			boolean sell = args[0].equalsIgnoreCase("sell");
			getShopOfersForItem(currentPlayer, args[1], page, sell);
			return true;
		}
		if (args[0].equalsIgnoreCase("adminshop")) {
			if (args.length < 2) {
				currentPlayer.sendMessage("§6Zu wenige Parameter: /ekz adminshop info");
				return true;
			}
			Material item = currentPlayer.getItemInHand().getType();
			ekz.getEkzRegister().shopAdminShopData(item);
			return true;
		}
		return false;							
    }


	private void getShopOfersForItem(Player currentPlayer, String ItemId, int page, boolean sell) {
		String ekzRegion = RegionHelper.getCurrentRegionName(currentPlayer);
		if (!ekz.getEkzRegister().ekzExist(ekzRegion)) {
			currentPlayer.sendMessage("§6Du befindest dich in keinem Einkaufszentrum!");
			return;
		}
		ekz.getEkzRegister().showItemList(currentPlayer, ekzRegion, ItemId, sell, page);
	}

	private void registerItemToShop(Player currentPlayer) {
		Block b = currentPlayer.getTargetBlock(null, 100);		
		if (b == null || b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST) {
			currentPlayer.sendMessage("§6Ziele auf das zu registierende Shop-Schild und versuch es nochmal!");
			return;
		}			
		
		ProtectedRegion region = RegionHelper.getBlockRegion(b);
		if (region == null) {
			currentPlayer.sendMessage("§6Das Schild befindet sich in keiner Region!");
			return;
		}
//		if (!RegionHelper.isOwnerOf(currentPlayer, region)) {
			//currentPlayer.sendMessage("§6Info: Dieser Shop gehört nicht dir!");
			//return true; // check if nessesary
//		}
		
		
		Sign sign = (Sign)b.getState();			
		if (!ChestShopSign.isValid(sign)) {
			currentPlayer.sendMessage("§6Das ist kein Shop-Schild!");
			return;
		}						
		
		String chestShopOwnerName = sign.getLine(0);
		if (!chestShopOwnerName.equalsIgnoreCase(currentPlayer.getName())) {
			currentPlayer.sendMessage("§6Das Shop-Schild ist nicht von dir!");
			return;
		}
		    	
		double buyPrice = PriceUtil.getBuyPrice(sign.getLine(2));
		double sellPrice = PriceUtil.getSellPrice(sign.getLine(2));
		ItemStack item = MaterialUtil.getItem(sign.getLine(3));
		String itemName = item.getType().toString();
		int amount = Integer.parseInt(sign.getLine(1));
		if (amount < 1) {
		    amount = 1;
		}
		item.setAmount(amount);
		if (ekz.getEkzRegister().addItem(region.getId(), chestShopOwnerName, item, buyPrice, sellPrice)) {
			currentPlayer.sendMessage("§6Angebot wurde registriert (" + amount + "x " + itemName + (buyPrice>0 ? " B:"+buyPrice : "")  + (sellPrice>0 ? " S:"+sellPrice : "") + ")");
		}
		else {
			currentPlayer.sendMessage("§6Angebot konnte nicht registriert werden!");
		}
	}

	private void showShopPages(Player currentPlayer, int page) {
		String ekzName = RegionHelper.getCurrentRegionName(currentPlayer);
		ekz.getEkzRegister().showShops(currentPlayer, ekzName, page);
	}
	
	private void showShopList(Player currentPlayer) {
		String ekzName = RegionHelper.getCurrentRegionName(currentPlayer);
		ekz.getEkzRegister().showShopList(currentPlayer, ekzName);
	}

	private void deleteShopById(CommandSender sender, String shopId) {
		String ekzRegion = RegionHelper.getCurrentRegionName((Player)sender);		
		if (!ekz.getEkzRegister().ekzExist(ekzRegion)) {	
			sender.sendMessage("§6Es wurde kein Einkaufszentrum mit diesem Namen gefunden!");
			return;
		}
		if (ekz.getEkzRegister().deleteShop(ekzRegion, shopId)) {
			sender.sendMessage("§6Shop wurde gelöscht.");
		}
	}

	private void addShopWithIdInRegion(CommandSender sender, String shopId, String shopRegion) {
		String ekzRegion = RegionHelper.getCurrentRegionName((Player)sender);
		if (ekzRegion.isEmpty()) {
			sender.sendMessage("§6Du befindest dich in keiner Region!");
			return;
		}
		if (!ekz.getEkzRegister().ekzExist(ekzRegion)) {
			sender.sendMessage("§6Du befindest dich in keinem Einkaufszentrum!");
			return;
		}
		if (ekz.getEkzRegister().addNewShop(ekzRegion, shopId, shopRegion)) {
			sender.sendMessage("§6Shop '" + shopId + "' in '" + shopRegion + "' wurde erstellt.");
		}
		else {
			sender.sendMessage("§6Shop '" + shopId + "' in '" + shopRegion + "' konnte nicht erstellt werden!");
		}
	}

	private void deleteEkzByRegionName(CommandSender sender, String ekzRegion) {
		if (!ekz.getEkzRegister().ekzExist(ekzRegion)) {				
			sender.sendMessage("§6Es wurde kein Einkaufszentrum mit diesem Namen gefunden!");
			return;
		}
		if (ekz.getEkzRegister().deleteEkz(ekzRegion)) {
			sender.sendMessage("§6Einkaufszentrum wurde gelöscht.");
		}
		else {
			sender.sendMessage("§6Einkaufszentrum konnte nicht gelöscht werden.");
		}
	}

	private void addEkzAtMyPosition(Player currentPlayer) {
		String ekzRegion = RegionHelper.getCurrentRegionName(currentPlayer);
		if (ekzRegion.isEmpty()) {
			currentPlayer.sendMessage("§6Du befindest dich in keiner Region!");
			return;
		}
		if (ekz.getEkzRegister().addNewEkz(ekzRegion)) {
			currentPlayer.sendMessage("§6Einkaufszentrum '" + ekzRegion + "' wurde erstellt.");
		}
		else {
			currentPlayer.sendMessage("§6Einkaufszentrum '" + ekzRegion + "' konnte nicht erstellt werden!");
		}
	}
}
