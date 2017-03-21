package cc.bukkitPlugin.commons.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.commons.Log;
import cc.commons.util.ClassUtil;
import cc.commons.util.StringUtil;

public class BukkitUtil{

    /** 是否拥有副手 */
    private final static boolean mHasBothHand;

    static{
        mHasBothHand=ClassUtil.isMethodExist(PlayerInventory.class,"getItemInMainHand");
    }

    /**
     * 获取主手上的物品
     * 
     * @param pPlayer
     *            玩家
     * @return 物品
     */
    public static ItemStack getItemInMainHand(Player pPlayer){
        if(BukkitUtil.mHasBothHand){
            return pPlayer.getInventory().getItemInMainHand();
        }else{
            return pPlayer.getItemInHand();
        }
    }

    /**
     * 设置主手上的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     */
    public static void setItemInMainHand(Player pPlayer,ItemStack pItem){
        if(BukkitUtil.mHasBothHand){
            pPlayer.getInventory().setItemInMainHand(pItem);
        }else{
            pPlayer.setItemInHand(pItem);
        }
    }

    /**
     * 获取在线玩家
     * 
     * @return 在线的玩家
     */
    public static Player[] getOnlinePlayers(){
        Method tMethod;
        try{
            tMethod=Bukkit.class.getDeclaredMethod("getOnlinePlayers");
            Object tObject=tMethod.invoke(null);
            Player[] tPlayers;
            if(tObject instanceof Player[])
                tPlayers=(Player[])tObject;
            else{
                Collection<Player> tcPlayers=(Collection<Player>)tObject;
                tPlayers=new Player[tcPlayers.size()];
                tcPlayers.toArray(tPlayers);
            }
            return tPlayers;
        }catch(Throwable exp){
            Log.severe("获取在线玩家列表时发生了错误",exp);
            return new Player[0];
        }
    }

    /**
     * 获取在线玩家名字列表
     * 
     * @return 在线玩家名字列表
     */
    public static ArrayList<String> getOnlinePlayersName(){
        ArrayList<String> tPlayerNames=new ArrayList<>();
        for(Player sPlayer : BukkitUtil.getOnlinePlayers())
            tPlayerNames.add(sPlayer.getName());
        return tPlayerNames;
    }

    /**
     * 获取所有离线的玩家
     * <p>
     * 数据较多时可能造成服务器卡顿
     * </p>
     * 
     * @return 离线的玩家
     */
    @Deprecated
    public static OfflinePlayer[] getOfflinePlayers(){
        Method tMethod;
        try{
            tMethod=Bukkit.class.getDeclaredMethod("getOfflinePlayers");
            Object tObject=tMethod.invoke(null);
            OfflinePlayer[] tPlayers;
            if(tObject instanceof OfflinePlayer[])
                tPlayers=(OfflinePlayer[])tObject;
            else if(tObject instanceof Collection){
                Collection<OfflinePlayer> tcPlayers=(Collection<OfflinePlayer>)tObject;
                tPlayers=new OfflinePlayer[tcPlayers.size()];
                tcPlayers.toArray(tPlayers);
            }else tPlayers=new OfflinePlayer[0];
            return tPlayers;
        }catch(Throwable exp){
            Log.severe("获取离线玩家列表时发生了错误",exp);
            return new OfflinePlayer[0];
        }
    }

    public static ArrayList<String> getOfflinePlayersName(){
        ArrayList<String> playerNames=new ArrayList<>();
        for(OfflinePlayer sPlayer : BukkitUtil.getOfflinePlayers())
            playerNames.add(sPlayer.getName());
        return playerNames;
    }

    public static boolean isItemMetaEmpty(ItemMeta pMeat){
        Method tMethod;
        try{
            if(pMeat==null)
                return true;
            tMethod=pMeat.getClass().getDeclaredMethod("isEmpty");
            tMethod.setAccessible(true);
            Boolean result=(Boolean)tMethod.invoke(pMeat);
            if(result==null||result)
                return true;
            else return false;
        }catch(Throwable exp){
            return true;
        }
    }

    /**
     * 安全的添加物品到玩家背包,如果玩家背包满了,会将物品丢弃到地上
     *
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     * @param pCount
     *            物品数量
     */
    public static void giveItem(Player pPlayer,ItemStack pItem,int pCount){
        if(!BukkitUtil.isValidItem(pItem)||pCount<=0)
            return;
        pItem=pItem.clone();
        pItem.setAmount(pCount);
        BukkitUtil.giveItem(pPlayer,pItem);
    }

    /**
     * 安全的添加物品到玩家背包,如果玩家背包满了,会返回未添加的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     * @return 为能添加到背包的物品
     */
    public static ItemStack giveItemWithoutDrop(Player pPlayer,ItemStack pItem){
        if(!BukkitUtil.isValidItem(pItem)||pPlayer==null)
            return null;

        pItem=pItem.clone();
        if(pPlayer.getInventory().firstEmpty()==-1){ // 背包满了
            if(pItem.getMaxStackSize()==1){
                return pItem;
            }
        }
        int tAllowCount=0;
        for(ItemStack sInvItem : pPlayer.getInventory().getContents()){
            if(BukkitUtil.isValidItem(sInvItem)){
                if(sInvItem.isSimilar(pItem)){
                    tAllowCount+=Math.max(0,sInvItem.getMaxStackSize()-sInvItem.getAmount());
                }
            }else{
                tAllowCount+=pItem.getMaxStackSize();
            }
            if(tAllowCount>=pItem.getAmount())
                break;
        }

        ItemStack tLeftItems=null;
        if(tAllowCount<pItem.getAmount()){
            tLeftItems=pItem.clone();
            tLeftItems.setAmount(pItem.getAmount()-tAllowCount);
            pItem.setAmount(tAllowCount);
        }

        int tMaxStackSize=pItem.getMaxStackSize();
        if(tMaxStackSize<=0)
            tMaxStackSize=1;
        for(int i=0;i<pItem.getAmount()/tMaxStackSize;i++){
            ItemStack tGiveItem=pItem.clone();
            tGiveItem.setAmount(tMaxStackSize);
            pPlayer.getInventory().addItem(tGiveItem);
        }
        if(tMaxStackSize>1){
            int tLeftAmount=pItem.getAmount()%tMaxStackSize;
            if(tLeftAmount!=0){
                ItemStack tGiveItem=pItem.clone();
                tGiveItem.setAmount(tLeftAmount);
                pPlayer.getInventory().addItem(tGiveItem);
            }
        }

        return tLeftItems;
    }

    /**
     * 安全的添加物品到玩家背包,如果玩家背包满了,会将物品丢弃到地上
     * <p>
     * 物品的数量由{@link ItemStack#getAmount}来决定
     * </p>
     *
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品
     */
    public static void giveItem(Player pPlayer,ItemStack pItem){
        if(!BukkitUtil.isValidItem(pItem)||pPlayer==null)
            return;

        pItem=pItem.clone();
        if(pPlayer.getInventory().firstEmpty()==-1){ // 背包满了
            if(pItem.getMaxStackSize()==1){
                BukkitUtil.dropItem(pPlayer.getLocation(),pItem);
                return;
            }
        }

        int tAllowCount=0;
        for(ItemStack sInvItem : pPlayer.getInventory().getContents()){
            if(BukkitUtil.isValidItem(sInvItem)){
                if(sInvItem.isSimilar(pItem)){
                    tAllowCount+=Math.max(0,sInvItem.getMaxStackSize()-sInvItem.getAmount());
                }
            }else{
                tAllowCount+=pItem.getMaxStackSize();
            }
            if(tAllowCount>=pItem.getAmount())
                break;
        }
        if(tAllowCount<pItem.getAmount()){
            ItemStack tDropItems=pItem.clone();
            tDropItems.setAmount(pItem.getAmount()-tAllowCount);
            pItem.setAmount(tAllowCount);
            BukkitUtil.dropItem(pPlayer.getLocation(),tDropItems);
        }

        int tMaxStackSize=pItem.getMaxStackSize();
        if(tMaxStackSize<=0)
            tMaxStackSize=1;
        for(int i=0;i<pItem.getAmount()/tMaxStackSize;i++){
            ItemStack giveItem=pItem.clone();
            giveItem.setAmount(tMaxStackSize);
            pPlayer.getInventory().addItem(giveItem);
        }
        if(tMaxStackSize>1){
            int tLeftAmount=pItem.getAmount()%tMaxStackSize;
            if(tLeftAmount!=0){
                ItemStack tGiveItem=pItem.clone();
                tGiveItem.setAmount(tLeftAmount);
                pPlayer.getInventory().addItem(tGiveItem);
            }
        }
    }

    /**
     * 生成掉落物 *
     * <p>
     * 物品的数量由{@link ItemStack#getAmount}来决定
     * </p>
     * 
     * @param pLoc
     *            位置
     * @param pItem
     *            物品
     */
    public static void dropItem(Location pLoc,ItemStack pItem){
        if(!BukkitUtil.isValidItem(pItem)||pLoc==null||pLoc.getWorld()==null)
            return;

        int tMaxstackSize=pItem.getMaxStackSize();
        if(tMaxstackSize<=0)
            tMaxstackSize=1;
        ItemStack tDropItem=pItem.clone();

        tDropItem.setAmount(tMaxstackSize);
        for(int i=0;i<pItem.getAmount()/tMaxstackSize;i++){
            pLoc.getWorld().dropItem(pLoc,tDropItem.clone());
        }
        if(tMaxstackSize>1){
            int tLeftAmount=pItem.getAmount()%tMaxstackSize;
            if(tLeftAmount!=0){
                tDropItem=pItem.clone();
                tDropItem.setAmount(tLeftAmount);
                pLoc.getWorld().dropItem(pLoc,tDropItem);
            }
        }
    }

    /**
     * 快速设置物品信息
     * 
     * @param pItem
     *            物品
     * @param pDisplayName
     *            名字
     * @param pLores
     *            Lore
     * @return 设置信息后的物品
     */
    public static ItemStack setItemInfo(ItemStack pItem,String pDisplayName,String...pLores){
        if(pItem==null)
            return null;

        ItemMeta tMeta=pItem.getItemMeta();
        tMeta.setDisplayName(StringUtil.isBlank(pDisplayName)?"":ChatColor.translateAlternateColorCodes('&',pDisplayName));

        if(pLores!=null&&pLores.length!=0){
            ArrayList<String> newLore=new ArrayList<>(pLores.length);
            for(String sSigleLore : pLores){
                if(sSigleLore==null)
                    sSigleLore="";
                newLore.add(ChatColor.translateAlternateColorCodes('&',sSigleLore));
            }
            tMeta.setLore(newLore);
        }

        pItem.setItemMeta(tMeta);
        return pItem;

    }

    /**
     * 物品不为null且不为空气
     * 
     * @param pItem
     *            物品
     * @return 是否是正常的物品
     */
    public static boolean isValidItem(ItemStack pItem){
        return pItem!=null&&pItem.getType()!=Material.AIR;
    }

    /**
     * 方块不为null且不为空气
     * 
     * @param pBlock
     *            方块
     * @return 是否是正常的方块
     */
    public static boolean isValidBlock(Block pBlock){
        return pBlock!=null&&pBlock.getType()!=Material.AIR;
    }

    /**
     * 获取物品类型
     * <p>
     * 根据所给的字符串获取物品类型,字符串可以为{@link Material}的枚举值,或者{@link Material#getId()}的值
     * </p>
     * 
     * @param pTypeStr
     *            物品类型字符串
     * @return 物品类型如果不存在则null
     */
    public static Material getItemType(String pTypeStr){
        pTypeStr=pTypeStr.trim();
        if(pTypeStr.matches("\\d+")){
            try{
                Material tMate=Material.getMaterial(Integer.parseInt(pTypeStr));
                if(tMate!=null&&tMate!=Material.AIR)
                    return tMate;
            }catch(NumberFormatException exp){
                return null;
            }
        }
        Material tMate=Material.getMaterial(pTypeStr.toUpperCase());
        return (tMate!=null&&tMate!=Material.AIR)?tMate:null;

    }
}
