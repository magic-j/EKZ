package com.magic_j.ekz;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.w3c.dom.Element;

import yt.codebukkit.scoreboardapi.Scoreboard;
import yt.codebukkit.scoreboardapi.ScoreboardAPI;

import com.magic_j.pluginhelpers.RegionHelper;

// http://forums.bukkit.org/threads/tutorial-scoreboards-teams-with-the-bukkit-api.139655/

public class EkzRegister {

	private EkzXmlData xmlData;
	//private List<Scoreboard> scoreboards = new ArrayList<Scoreboard>();
	private Map<Player, ScorePage> playerBoards = new HashMap<Player, ScorePage>();
	
	private final int maxItemsPerPage = 15;
	
	public EkzRegister() {
		xmlData = new EkzXmlData();
		xmlData.load("plugins\\data.xml");	
	}
	
	public void close() {
		xmlData.save();
	}

	public void showShops(Player p, String ekzName, int page) {
		if (page == 0) {
			if (playerBoards.containsKey(p)) {
				ScorePage oldPage = playerBoards.get(p);
				if (oldPage.type.equals("S_")) {
					page = oldPage.page + 1;
				}
			}
			else {
				page = 1;
			}
		}
		
		Map<Integer, String> shops = xmlData.getShopList(ekzName, p.getWorld(), false);
		if (shops == null || shops.size() == 0) {
			p.sendMessage(ChatColor.GOLD + "Es wurden noch keine Shops eingetragen!");
			return;
		}
		
		int pageAnz = (int) Math.ceil(shops.size()/(double)maxItemsPerPage);
		ScorePage newEkzPage = new ScorePage(ekzName, Math.max(page, 1), "S_");
		newEkzPage.page = Math.min(newEkzPage.page, pageAnz);		
		String newBoardId = str16(newEkzPage.getBoardId());
		
		if (playerBoards.containsKey(p)) {
			String oldBoardId = str16(playerBoards.get(p).getBoardId());
			Scoreboard board = ScoreboardAPI.getInstance().getScoreboard(oldBoardId);
			if (board == null) {
				return;
			}
			board.showToPlayer(p, false);
			playerBoards.remove(p);
			if (newBoardId.equals(oldBoardId)) {
				return;
			}
		}		
		
		Scoreboard shopPage = ScoreboardAPI.getInstance().getScoreboard(newBoardId);
		if (shopPage == null) {
			shopPage = ScoreboardAPI.getInstance().createScoreboard(newBoardId, 3);
			//p.sendMessage(ChatColor.GOLD + "new BoardId: " + newBoardId);
			shopPage.setType(Scoreboard.Type.SIDEBAR);
			shopPage.setScoreboardName(ChatColor.GOLD + "Shops (§e" + newEkzPage.page + "/" + pageAnz + ChatColor.GOLD + ") §cNr.");
		}
		else {
			shopPage.clearItems();
		}
		
		int begin = 1 + (newEkzPage.page-1) * maxItemsPerPage; 
		int end  = begin + maxItemsPerPage - 1;
		end = Math.min(end, shops.size());
		Object[] names = shops.keySet().toArray();
		for (int i = begin; i <= end; i++) {
			Integer id = (Integer)names[i-1];
			String shopMembers = RegionHelper.getshopMembersByRegionName(shops.get(id), p.getWorld());
			if (shopMembers == null) {
				p.sendMessage("Region " + shops.get(id) + " wurde nicht gefunden!");
				continue;
			}
			shopPage.setItem(str16(shopMembers), id);
		}
		
		//updateForAllPlayers(shopPage);
		shopPage.showToPlayer(p);
		playerBoards.put(p, newEkzPage);		
	}

	public boolean ekzExist(String ekzName) {
		return xmlData.existEkz(ekzName);
	}

	public boolean addNewShop(String ekzRegion, String shopId, String shopRegion) {
		return xmlData.addShop(ekzRegion, shopId, shopRegion);
	}

	public boolean addNewEkz(String ekzName) {
		return xmlData.addEkz(ekzName);
	}

	public String getEkzNames() {
		return xmlData.getEkzNames();
	}

	public boolean addItem(String shopRegion, String shopOwnerName, ItemStack item, double buy, double sell) {
		Element shopNode = xmlData.getShopNode(shopRegion);
		String itemId = Integer.toString(item.getTypeId());
		int count = item.getAmount();
		MaterialData data = item.getData();
		
		if (data != null && data.getData() != 0) {
			itemId += ":" + data.getData();
		}
		return xmlData.addItem(shopNode, shopOwnerName, itemId, count, sell, buy, item.getType().toString());
	}
	

	public boolean delItem(String shopRegion, String shopOwnerName, ItemStack item) {
		Element shopNode = xmlData.getShopNode(shopRegion);
		String itemId = Integer.toString(item.getTypeId());		
		return xmlData.removeItem(shopNode, shopOwnerName, itemId);		
	}
	
	public boolean deleteShop(String ekzName, String shopId) {
		return xmlData.removeShop(ekzName, shopId);
	}

	public String getItemName(String matName) {
		int splitPos = matName.lastIndexOf(':');
		String data = "";
		if (splitPos > 0) {
			data = matName.substring(splitPos);		
			matName = matName.substring(0, splitPos);				
		}
		matName = matName.replace(" ", "_").toUpperCase();
		Material mat = Material.getMaterial(matName);
		if (mat != null) {
			return mat.name() + data;
		}
		return "";
	}

	public void shopAdminShopData(Material mat) {
		
	}
  
    private String str16(String text) {
    	return text.substring(0, Math.min(16, text.length()));
    }

	public void showItemList(Player p, String ekzRegion, String itemId, boolean sell, int page) {
		if (page == 0) {
			if (playerBoards.containsKey(p)) {
				ScorePage oldPage = playerBoards.get(p);
				if (!oldPage.type.equals("S_")) {
					page = oldPage.page + 1;
				}
			}
			else {
				page = 1;
			}
		}		
		
		Element ekzNode = xmlData.getEkzNode(ekzRegion);
		List<Element> items = xmlData.getItemsWithId(ekzNode, itemId);
		
		DecimalFormat f = new DecimalFormat("#0.00"); 
		
		Map<String, Integer> itemMap = new HashMap<String, Integer>();		
		for (int i = 0; i < items.size(); i++) {
			Element element = items.get(i);
			String sValue = element.getAttribute("s");
			String bValue = element.getAttribute("b");
			String count = element.getAttribute("c");
			double val = 0.0;
			int c = 64;
			try {
				val = Double.parseDouble(sell ? sValue : bValue);
				c = Integer.parseInt(count);
			}
			catch (NumberFormatException e) {
				Logger.getLogger("Minecraft").info("'" + (sell ? sValue : bValue) + "' is not a Double!");
			}
			if (val > 0.0) {
				
				//String owner = xmlData.getItemOwner(element);
				String shopId = xmlData.getItemShopId(element);
				double stackVal = val / (double)c * 64.0;
				// \u03A1\u2119\u204B\u2117\u00DE
				String text = f.format(val) + "/" + count + " [" + f.format(stackVal) + "]";
				itemMap.put(str16(text), Integer.parseInt(shopId));
			}
		}
		if (itemMap.size() == 0) {
			p.sendMessage(ChatColor.GOLD + "Es wurden keine passende Angebote gefunden!");
			return;
		}
				
		int iId = 0;
		Material mat = Material.getMaterial(itemId);
		if (mat != null) {
			iId = mat.getId();
		}
		int pageAnz = (int) Math.ceil(itemMap.size()/(double)maxItemsPerPage);
		ScorePage newEkzPage = new ScorePage(ekzRegion, Math.max(page, 1), iId + (sell ? "S" : "B"));
		newEkzPage.page = Math.min(newEkzPage.page, pageAnz);		
		String newBoardId = str16(newEkzPage.getBoardId());
		
		if (playerBoards.containsKey(p)) {
			String oldBoardId = str16(playerBoards.get(p).getBoardId());
			Scoreboard board = ScoreboardAPI.getInstance().getScoreboard(oldBoardId);
			if (board == null) {
				return;
			}
			board.showToPlayer(p, false);
			playerBoards.remove(p);
			if (newBoardId.equals(oldBoardId)) {
				return;
			}
		}	
		
		Scoreboard itemsPage = ScoreboardAPI.getInstance().getScoreboard(newBoardId);
		if (itemsPage == null) {
			itemsPage = ScoreboardAPI.getInstance().createScoreboard(newBoardId, 3);
			//p.sendMessage(ChatColor.GOLD + "new BoardId: " + newBoardId);
			itemsPage.setType(Scoreboard.Type.SIDEBAR);
			itemsPage.setScoreboardName(ChatColor.GOLD + "Items (§e" + newEkzPage.page + "/" + pageAnz + ChatColor.GOLD + ") §cShopNr.");
		}
		else {
			itemsPage.clearItems();
		}
		
		int begin = 1 + (newEkzPage.page-1) * maxItemsPerPage; 
		int end  = begin + maxItemsPerPage - 1;
		end = Math.min(end, itemMap.size());
		Object[] names = itemMap.keySet().toArray();
		for (int i = begin; i <= end; i++) {
			String name = (String)names[i-1];
			itemsPage.setItem(name, itemMap.get(name));			
		}
		
		//updateForAllPlayers(itemsPage);
		itemsPage.showToPlayer(p);			
		playerBoards.put(p, newEkzPage);
	}

	public boolean deleteEkz(String ekzName) {
		return xmlData.removeEkz(ekzName);
	}

	public void showShopList(Player p, String ekzName) {
		Map<Integer, String> shops = xmlData.getShopList(ekzName, p.getWorld(), true);
		if (shops == null || shops.size() == 0) {
			p.sendMessage(ChatColor.GOLD + "Es wurden noch keine Shops eingetragen!");
			return;
		}
		Set<Integer> keys = shops.keySet();	    
		for (Integer shopId : keys) {
			String region = shops.get(shopId);
			p.sendMessage(shopId + " " + shops.get(shopId) + " [" + RegionHelper.getshopMembersByRegionName(region, p.getWorld()) + "]");
		}
	}

}
