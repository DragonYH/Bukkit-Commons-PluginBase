package cc.bukkitPlugin.commons.plugin.manager.fileManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.commons.util.StringUtil;

public class TLangManager<T extends ABukkitPlugin<T>>extends AFileManager<T> implements INeedReload{

    protected final static HashMap<String,String> mDefaultLang=new HashMap<>();
    protected static TLangManager<?> mInstance;

    private final LinkedHashMap<Class<? extends ILangModel>,ILangModel> mLangModel=new LinkedHashMap<>();

    static{
        TLangManager.mDefaultLang.put("MsgAtPosition","在位置");
        TLangManager.mDefaultLang.put("MsgAllModelHasReadConfig","所有模块已经读入配置");
        TLangManager.mDefaultLang.put("MsgAsyncCommandNotAllow","不允许异步执行命令");
        TLangManager.mDefaultLang.put("MsgConfigReloaded","已经重载插件配置");
        TLangManager.mDefaultLang.put("MsgConsoleNotAllow","§c控制台不允许执行该命令");
        TLangManager.mDefaultLang.put("MsgErrorArg","§c错误的参数");
        TLangManager.mDefaultLang.put("MsgErrorArgsNumber","§c错误的参数数量");
        TLangManager.mDefaultLang.put("MsgErrorHappedWhenHandlerCmd","§c在处理命令时发生了错误");
        TLangManager.mDefaultLang.put("MsgErrorHappendPleaseContanctAdmin","§c插件发生了内部错误,请联系管理员");
        TLangManager.mDefaultLang.put("MsgErrorHappendWhenReloadConfig","§c在重载插件时发生了错误");
        TLangManager.mDefaultLang.put("MsgErrorHappendWhenReloadLang","§c在重载语言列表时发生了错误");
        TLangManager.mDefaultLang.put("MsgGetHelp","来获取帮助");
        TLangManager.mDefaultLang.put("MsgGetHelp","获取帮助");
        TLangManager.mDefaultLang.put("MsgLangReloaded","已经重载语言翻译");
        TLangManager.mDefaultLang.put("MsgLoadFileFail","§c载入%file%失败,目标文件以及配置未变动");
        TLangManager.mDefaultLang.put("MsgMissingLangNode","§c丢失语言节点 [%node%]");
        TLangManager.mDefaultLang.put("MsgNoPermitDoThisCommand","§c没有权限执行该命令");
        TLangManager.mDefaultLang.put("MsgLackPermitDoThisCommand","§c你需要 §4§l%permission% §c的权限才能执行该命令");
        TLangManager.mDefaultLang.put("MsgSwitchDebugOnCfgToSeeErrorStack","§c配置文件启用调试来看到更多错误消息");
        TLangManager.mDefaultLang.put("MsgPluginReloaded","已经重载插件");
        TLangManager.mDefaultLang.put("MsgSaveFileFail","§c保存数据到%file%失败");
        TLangManager.mDefaultLang.put("MsgUnInstanceManager","§c未实例化管理器类%class%");
        TLangManager.mDefaultLang.put("MsgUnknowChildCommand","§c未知的子命令");
        TLangManager.mDefaultLang.put("MsgUnknowCommand","§c未知的命令");
        TLangManager.mDefaultLang.put("WordInput","输入");
        TLangManager.mDefaultLang.put("WordError","错误");
        TLangManager.mDefaultLang.put("WordWarn","警告");
        TLangManager.mDefaultLang.put("WordDebug","调试");
    }

    /**
     * 获取指定消息节点的翻译,大小写敏感
     * <p>
     * 获取优先级: 实例中的语言->自定义默认语言(非空时)->默认的语言->Node本身<br>
     * 注意,本函数中不会报节点丢失错误<br>
     * 请只使用该函数获取默认的语言
     * </p>
     * 
     * @param pNode
     *            消息节点名
     * @param pDefLang
     *            默认的语言,如果为null时,默认值将返回pNode
     * @return 翻译后的消息
     */
    public static String staticGetNode(String pNode,String pDefLang){
        if(TLangManager.mInstance!=null){
            String tLang=TLangManager.mInstance.mConfig.getString(pNode);
            if(StringUtil.isNotEmpty(tLang)){
                return ChatColor.translateAlternateColorCodes('&',tLang);
            }
        }
        String tLang=TLangManager.mDefaultLang.get(pNode);
        if(tLang!=null){
            return ChatColor.translateAlternateColorCodes('&',tLang);
        }else{
            return StringUtil.isNotEmpty(pDefLang)?pDefLang:pNode;
        }
    }

    public static String getOpenDebugLang(){
        return TLangManager.staticGetNode("MsgSwitchDebugOnCfgToSeeErrorStack","§c配置文件设置LogLevel为DEBUG或以上等级来看到更多错误消息");
    }

    public TLangManager(T pPlugin,String pFileName,String pVersion){
        super(pPlugin,pFileName,pVersion);
        TLangManager.mInstance=this;
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            if(this.getClass()==TLangManager.class) // 如果没有进行继承
                Log.severe(pSender,TLangManager.staticGetNode("MsgErrorHappendWhenReloadLang",null));
            return false;
        }
        if(this.getClass()==TLangManager.class){ // 如果没有进行继承,则本函数添加默认
            this.addDefaults();
            this.reloadModles();
            this.saveConfig(null);
            Log.info(pSender,TLangManager.staticGetNode("MsgLangReloaded",null));
        }
        return true;
    }

    protected void reloadModles(){
        for(ILangModel sModel : this.mLangModel.values()){
            sModel.setLang(this.mConfig);
        }
    }

    /**
     * 获取指定消息节点的翻译,大小写敏感
     * 
     * @param pNode
     *            消息节点名
     * @return 翻译后的消息
     */
    public String getNode(String pNode){
        String tMsg=this.mConfig.getString(pNode);
        if(tMsg!=null){
            return ChatColor.translateAlternateColorCodes('&',tMsg);
        }else{
            Log.warn(TLangManager.staticGetNode("MsgMissingLangNode",null).replace("%node%",pNode));
            return pNode;
        }
    }

    /**
     * 获取指定消息节点的翻译,大小写敏感,同时翻译占位符
     * 
     * @param pNode
     *            节点名
     * @param pPlaceHolders
     *            占位符列表
     * @param pParams
     *            翻译替代语言
     * @return 翻译后的消息
     */
    public String getNode(String pNode,String[] pPlaceHolders,Object...pParams){
        String tLang=this.getNode(pNode);
        if(tLang.equals(pNode))
            return tLang;

        if(pPlaceHolders.length!=pParams.length)
            throw new IllegalArgumentException("占位符数量必须与翻译替代语言数量一致");

        for(int i=0;i<pPlaceHolders.length;i++){
            tLang=tLang.replace(pPlaceHolders[i],String.valueOf(pParams[i]));
        }
        return tLang;
    }

    /**
     * 获取指定消息节点的翻译,大小写敏感
     * 
     * @param pNode
     *            消息节点名
     * @param pDefValue
     *            消息未找到时的默认消息
     * @return 翻译后的消息
     */
    public String getNode(String pNode,String pDefValue){
        String tMsg=this.mConfig.getString(pNode);
        if(tMsg!=null){
            return ChatColor.translateAlternateColorCodes('&',tMsg);
        }else{
            Log.warn(TLangManager.staticGetNode("MsgMissingLangNode",null).replace("%node%",pNode));
            return StringUtil.isEmpty(pDefValue)?pDefValue:ChatColor.translateAlternateColorCodes('&',pDefValue);
        }
    }

    /**
     * 注册翻译模块到语言配置器
     * <p>
     * 一个类只能注册一个实例
     * </p>
     * 
     * @param pLangModel
     *            可翻译模块
     * @return 被替换的模块
     */
    public ILangModel registerLangModel(ILangModel pLangModel){
        if(pLangModel==null)
            return null;
        return this.mLangModel.put(pLangModel.getClass(),pLangModel);
    }

    /**
     * 从配置管理器中移除翻译模块到语言配置器
     * 
     * @param pClazz
     *            翻译模块类
     * @return 被移除的模块
     */
    public ILangModel unregisterLangModel(Class<? extends ILangModel> pClazz){
        if(pClazz==null)
            return null;
        return this.mLangModel.remove(pClazz);
    }

    @Override
    protected void addDefaults(){
        // 写入默认语言
        for(Map.Entry<String,String> sEntry : TLangManager.mDefaultLang.entrySet()){
            this.mConfig.addDefault(sEntry.getKey(),sEntry.getValue());
        }
        // 获取注册模块的翻译
        for(ILangModel sModel : this.mLangModel.values()){
            sModel.addDefaultLang(this.mConfig);
        }
    }

    public void reloadModel(Class<? extends ILangModel> pClazz){
        ILangModel tModel=this.mLangModel.get(pClazz);
        if(tModel==null)
            return;

        tModel.addDefaultLang(this.mConfig);
        this.saveConfig(null);
    }

    protected void sortLang(){
        TreeSet<String> keyConvertTree=new TreeSet<>();
        keyConvertTree.addAll(this.mConfig.getKeys(true));
        for(String sLangKey : keyConvertTree){
            this.mConfig.set(sLangKey,this.mConfig.removeCommentedValue(sLangKey));
        }
    }
}
