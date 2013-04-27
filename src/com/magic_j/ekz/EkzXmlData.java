package com.magic_j.ekz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bukkit.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.magic_j.pluginhelpers.RegionHelper;

public class EkzXmlData {
	
	private Document data;
	private String fileName;
	
	public EkzXmlData() {
		
	}
	
	public void load(String fileName) {
		this.fileName = fileName;
		try {			 
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			if (fXmlFile.exists()) {
				data = dBuilder.parse(fXmlFile);
				System.out.println(data.getClass().toString());
				data.getDocumentElement().normalize();	
			}
			else {
				data = dBuilder.newDocument();
				Element rootElement = data.createElement("data");
				data.appendChild(rootElement);				
			}			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public void save() {
		data.getDocumentElement().normalize();		
		try {			 
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(data);
			File fXmlFile = new File(fileName);
			StreamResult result = new StreamResult(fXmlFile);
			transformer.transform(source, result); 
			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public Element getRoot() {
		NodeList datas = data.getElementsByTagName("data");
		if (datas.getLength() < 1) {
			return null;
		}
		return (Element) datas.item(0);
	}
	public Element getEkzNode(String ekzName) {
		NodeList ekzDataList = data.getElementsByTagName("ekz");
		for (int i = 0; i < ekzDataList.getLength(); i++) {	
			Node nNode = ekzDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element ekzEl = (Element) nNode;
				if (ekzEl.getAttribute("name").equalsIgnoreCase(ekzName)) {
					return ekzEl;
				}
			}
		}
		return null;
	}
	public Element getShopNode(Element ekzNode, String shopId) {
		NodeList shopDataList = ekzNode.getElementsByTagName("shop");
		for (int i = 0; i < shopDataList.getLength(); i++) {	
			Node nNode = shopDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element shopEl = (Element) nNode;
				if (shopEl.getAttribute("id").equalsIgnoreCase(shopId)) {
					return shopEl;
				}
			}
		}
		return null;
	}
	public Element getShopNode(String shopRegion) {
		NodeList ekzDataList = data.getElementsByTagName("ekz");
		for (int i = 0; i < ekzDataList.getLength(); i++) {	
			Node nNode = ekzDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element ekzEl = (Element) nNode;
				NodeList shopDataList = ekzEl.getElementsByTagName("shop");
				for (int j = 0; j < shopDataList.getLength(); j++) {	
					Node sNode = shopDataList.item(j);
					if (sNode.getNodeType() == Node.ELEMENT_NODE) {	
						Element shopEl = (Element) sNode;
						if (shopEl.getAttribute("rg").equalsIgnoreCase(shopRegion)) {
							return shopEl;
						}
					}
				}
			}
		}
		return null;
	}
	public Element getItemNode(Element shopNode, String shopOwner, String itemId) {
		NodeList itemDataList = shopNode.getElementsByTagName("item");
		for (int i = 0; i < itemDataList.getLength(); i++) {	
			Node nNode = itemDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element itemEl = (Element) nNode;
				if (itemEl.getAttribute("id").equalsIgnoreCase(itemId)) {
					String owner = itemEl.getAttribute("own");
					if(!shopOwner.isEmpty() && owner != null && !owner.isEmpty()) {
						if (owner.equalsIgnoreCase(shopOwner)) {
							return itemEl;
						}
					}
					else {
						return itemEl;
					}
				}
			}
		}
		return null;
	}
	public Vector<Element> getItemNodes(Element shopNode, String itemId) {
		Vector<Element> itemNodes = new Vector<Element>();
		NodeList itemDataList = shopNode.getElementsByTagName("item");
		for (int i = 0; i < itemDataList.getLength(); i++) {	
			Node nNode = itemDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element itemEl = (Element) nNode;
				if (itemEl.getAttribute("id").equalsIgnoreCase(itemId)) {
					itemNodes.add(itemEl);
				}
			}
		}
		return itemNodes;
	}	
	public String getEkzNames() {
		String ekzNames = "";
		NodeList ekzDataList = data.getElementsByTagName("ekz");
		for (int i = 0; i < ekzDataList.getLength(); i++) {		 
			Node nNode = ekzDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element ekzEl = (Element) nNode;
				if (ekzNames.length() > 0) ekzNames += ", ";
				ekzNames += ekzEl.getAttribute("name");			 
			}
		}
		return ekzNames;
	}	
	public String getShopIDs(String ekzName) {
		String shopIDs = "";
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode != null) {
			NodeList shopDataList = ekzNode.getElementsByTagName("shop");
			for (int i = 0; i < shopDataList.getLength(); i++) {	
				Node nNode = shopDataList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element shopEl = (Element) nNode;
					if (shopIDs.length() > 0) shopIDs += ", ";
					shopIDs += shopEl.getAttribute("id");
				}
			}
		}
		return shopIDs;
	}

	public Element getShopByOwner(String ekzName, String ownerName) {
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode != null && !ownerName.isEmpty()) {
			NodeList shopDataList = ekzNode.getElementsByTagName("shop");
			for (int i = 0; i < shopDataList.getLength(); i++) {	
				Node nNode = shopDataList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
					Element shopEl = (Element) nNode;
					if (shopEl.getAttribute("owner").equalsIgnoreCase(ownerName)) {
						//shopId = shopEl.getAttribute("id");
						return shopEl;
					}
				}
			}
		}		
		return null;
	}
	
	public boolean existEkz(String ekzName) {
		Element ekzNode = getEkzNode(ekzName);
		return (ekzNode != null);
	}
	public boolean existShop(String ekzName, String shopId) {
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode == null) {
			return false;
		}
		Element shopNode = getShopNode(ekzNode, shopId);
		return (shopNode != null);
	}
	
	public boolean addEkz(String ekzName) {
		Element root = getRoot();
		if (root == null) {
			return false;
		}
		if (getEkzNode(ekzName) != null) {
			return false;
		}		
		Element ekzNode = data.createElement("ekz");
		ekzNode.setAttribute("name", ekzName);
		root.appendChild(ekzNode);
        return true;
	}
	public boolean removeEkz(String ekzName) {
		Element root = getRoot();
		Element ekzNode = getEkzNode(ekzName);
		if (root == null || ekzNode == null) {
			return false;
		}
		root.removeChild(ekzNode);
		return true;
	}
 	
	public boolean addShop(String ekzName, String shopID, String region) {
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode == null) {
			return false;
		}
		if (getShopNode(ekzNode, shopID) != null) {
			return false;
		}
        Element shopNode = data.createElement("shop");
        shopNode.setAttribute("id", shopID);
        shopNode.setAttribute("rg", region);
        //shopNode.setAttribute("owner", playerName);
        ekzNode.appendChild(shopNode);
        return true;		
	}
 	public boolean removeShop(String ekzName, String shopId) {
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode == null) {
			return false;
		}
		Element shopNode = getShopNode(ekzNode, shopId);
		if (shopNode == null) {
			return false;			
		}
		ekzNode.removeChild(shopNode);
		return true;
	}
	
 	public boolean addItem(String ekzName, String chestShopOwnerName, String shopID, String itemId, int count, double sell, double buy, String itemName) {
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode == null) {
			return false;
		}		
		Element shopNode = getShopNode(ekzNode, shopID);
		if (shopNode == null) {
			return false;
		}
		if (getItemNode(shopNode, "", itemId) != null) {
			return false;
		}
		return addItem(shopNode, chestShopOwnerName, itemId, count, sell, buy, itemName);
	}

	public boolean addItem(Element shopNode, String chestShopOwnerName, String itemId, int count, double sell, double buy, String itemName) {
		if (shopNode == null) {
			return false;
		}
		Element itemNode = getItemNode(shopNode, chestShopOwnerName, itemId);
		if (itemNode == null) {
			itemNode = data.createElement("item");
			shopNode.appendChild(itemNode);
		}
        itemNode.setAttribute("id", itemId);
        itemNode.setAttribute("own", chestShopOwnerName);
        itemNode.setAttribute("c", Integer.toString(count));
        itemNode.setAttribute("s", Double.toString(sell));
        itemNode.setAttribute("b", Double.toString(buy));
        itemNode.setAttribute("name", itemName);        
		return true;
	}
 	
	public boolean removeItem(Element shopNode, String chestShopOwnerName, String itemID) {
		if (shopNode == null) {
			return false;			
		}
		Element itemNode = getItemNode(shopNode, chestShopOwnerName, itemID);
		if (itemNode == null) {
			return false;			
		}
		shopNode.removeChild(itemNode);
		return true;
	}

	public String showShopList(String ekzName) {
/*		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode != null && !ownerName.isEmpty()) {
			NodeList shopDataList = ekzNode.getElementsByTagName("shop");
			for (int i = 0; i < shopDataList.getLength(); i++) {	
				Node nNode = shopDataList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
					Element shopEl = (Element) nNode;
					if (shopEl.getAttribute("owner").equalsIgnoreCase(ownerName)) {
						//shopId = shopEl.getAttribute("id");
						return shopEl;
					}
				}
			}
		}
*/
		return "- noch nicht implementiert -";
	}

	public Map<Integer, String> getShopList(String ekzName, World world, boolean empty) {		
		Element ekzNode = getEkzNode(ekzName);
		if (ekzNode == null) {
			return null;
		}
		Map<Integer, String> map = new HashMap<Integer, String>();
		NodeList shopDataList = ekzNode.getElementsByTagName("shop");
		for (int i = 0; i < shopDataList.getLength(); i++) {	
			Node nNode = shopDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element shopEl = (Element) nNode;				
				String shopRegion = shopEl.getAttribute("rg");
				String members = RegionHelper.getshopMembersByRegionName(shopRegion, world);
				if (empty || members != null && !members.isEmpty()) {
					int shopId = Integer.parseInt(shopEl.getAttribute("id"));
					map.put(shopId, shopRegion);
				}
			}
		}
		return map;		
	}

	public List<Element> getItemsWithId(Element ekzNode, String itemId) {
		List<Element> items = new ArrayList<Element>();
		NodeList shopDataList = ekzNode.getElementsByTagName("shop");
		for (int i = 0; i < shopDataList.getLength(); i++) {	
			Node nNode = shopDataList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	
				Element shopEl = (Element) nNode;
				NodeList itemDataList = shopEl.getElementsByTagName("item");
				for (int j = 0; j < itemDataList.getLength(); j++) {	
					Node iNode = itemDataList.item(j);
					if (iNode.getNodeType() == Node.ELEMENT_NODE) {	
						Element itemEl = (Element) iNode;						
						String itemName = itemEl.getAttribute("id");
						int splitPos = itemName.lastIndexOf(':');
						if (splitPos > 0) {
							itemName = itemName.substring(0, splitPos);				
						}						
						if (itemName.equalsIgnoreCase(itemId)) {
							items.add(itemEl);	
						}						
					}
				}
			}
		}
		return items;
	}

	public String getItemOwner(Element itemEl) {
		Node parent = itemEl.getParentNode();
		return ((Element)parent).getAttribute("owner");
	}

	public String getItemShopId(Element itemEl) {
		Node parent = itemEl.getParentNode();
		return ((Element)parent).getAttribute("id");
	}

}
