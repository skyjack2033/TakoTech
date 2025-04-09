package moe.takochan.takotech.common.item.ae;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.item.OreHelper;
import appeng.util.item.OreReference;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.takochan.takotech.common.item.BaseAECellItem;
import moe.takochan.takotech.common.storage.ITakoCellInventory;
import moe.takochan.takotech.common.storage.ITakoCellInventoryHandler;
import moe.takochan.takotech.common.storage.inventory.OreStorageCellInventory;
import moe.takochan.takotech.constants.NameConstants;
import moe.takochan.takotech.utils.CommonUtils;
import moe.takochan.takotech.utils.I18nUtils;

/**
 * 粗矿存储元件
 * <p>
 * 只接受带有"rawOre"矿典前缀的物品（粗矿）
 */
public class ItemRawOreStorageCell extends BaseAECellItem implements IStorageCell, IItemGroup {

    private static final String[] ORE_PREFIXES = { "rawOre" };
    private static String oreDefs;

    private final int perType = 1;
    private final double idleDrain;

    @SuppressWarnings("Guava")
    public ItemRawOreStorageCell() {
        super(NameConstants.ITEM_RAW_ORE_STORAGE_CELL);

        idleDrain = 1.14;

        this.setMaxStackSize(1);
        this.setTextureName(CommonUtils.resource(NameConstants.ITEM_RAW_ORE_STORAGE_CELL));
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    /**
     * 获取物品组未本地化字符串
     *
     * @param otherItems 其他物品
     * @param is         物品堆栈
     * @return 物品组未本地化字符串
     */
    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    /**
     * 添加物品的详细信息
     *
     * @param itemStack       物品堆栈
     * @param player          玩家实体
     * @param lines           显示的详细信息
     * @param displayMoreInfo 是否显示更多信息
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> lines,
        final boolean displayMoreInfo) {
        lines.add(I18nUtils.tooltip(NameConstants.ITEM_RAW_ORE_STORAGE_CELL_DESC)); // 添加物品的描述
        lines.add("§a仅存储粗矿 (rawOre)"); // 添加专用描述

        // 获取物品堆栈关联的存储单元库存处理器
        final IMEInventoryHandler<?> inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(itemStack, null, StorageChannel.ITEMS);

        // 检查库存处理器是否是 ICellInventoryHandler 类型
        if (inventory instanceof ITakoCellInventoryHandler handler) {
            final ITakoCellInventory cellInventory = handler.getCellInv(); // 获取存储单元的库存

            if (cellInventory != null) {
                if (!cellInventory.getDiskID()
                    .isEmpty()) {
                    lines.add(cellInventory.getDiskID());
                }

                // 显示已存储的物品类型数量和总物品类型数量
                lines.add(
                    NumberFormat.getInstance()
                        .format(cellInventory.getStoredItemTypes()) + " "
                        + GuiText.Of.getLocal()
                        + ' '
                        + NumberFormat.getInstance()
                            .format(this.getTotalTypes(itemStack))
                        + ' '
                        + GuiText.Types.getLocal());

                // 如果存储单元启用了预格式化
                if (handler.isPreformatted()) {
                    String filter = cellInventory.getOreFilter(); // 获取矿物过滤器
                    if (filter.isEmpty()) {
                        // 显示包含或排除模式
                        final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST
                            ? GuiText.Included
                            : GuiText.Excluded).getLocal();

                        // 判断是否启用了模糊模式
                        if (handler.isFuzzy()) {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
                        } else {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                        }

                        // 如果 Shift 键被按下，显示过滤器详细信息
                        if (GuiScreen.isShiftKeyDown()) {
                            lines.add(GuiText.Filter.getLocal() + ": ");
                            for (int i = 0; i < cellInventory.getConfigInventory()
                                .getSizeInventory(); ++i) {
                                ItemStack s = cellInventory.getConfigInventory()
                                    .getStackInSlot(i);
                                if (s != null) {
                                    lines.add(s.getDisplayName()); // 显示物品名称
                                }
                            }
                        }
                    } else {
                        // 显示已设置的矿物过滤器
                        lines.add(GuiText.PartitionedOre.getLocal() + " : " + filter);
                    }
                }

                String defs = getOreDefs();
                if (defs != null && !defs.isEmpty()) {
                    lines.add(I18nUtils.tooltip(NameConstants.ITEM_RAW_ORE_STORAGE_CELL_DESC) + ".1: ");
                    lines.add(defs);
                }
            }
        }

        super.addCheckedInformation(itemStack, player, lines, displayMoreInfo); // 调用父类方法，添加其他信息
    }

    /**
     * 获取该存储单元可用的字节大小。
     *
     * @param cellItem 存储单元物品
     * @return 无限容量，返回最大值
     */
    @Override
    public int getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    /**
     * 获取该存储单元的可用字节大小（返回长整型值）。
     *
     * @param cellItem 存储单元物品
     * @return 无限容量，返回最大值
     */
    @Override
    public long getBytesLong(final ItemStack cellItem) {
        return Long.MAX_VALUE;
    }

    /**
     * 每种物品类型占用的字节数。
     *
     * @param cellItem 存储单元物品
     * @return 每种类型占用的字节数
     */
    @Override
    public int BytePerType(ItemStack cellItem) {
        return this.perType;
    }

    /**
     * 获取每种类型的字节大小。
     *
     * @param cellItem 存储单元物品
     * @return 每种类型的字节大小
     */
    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    /**
     * 获取该存储单元可以存储的物品类型数量。
     *
     * @param cellItem 存储单元物品
     * @return 可存储的物品类型数量
     */
    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 63;
    }

    /**
     * 判断物品是否可以被存储在该存储单元中
     *
     * @param cellItem          存储单元物品
     * @param requestedAddition 请求添加的物品堆栈
     * @return 如果可以存储则返回 false，否则返回 true
     */
    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        if (requestedAddition != null) {
            ItemStack stack = requestedAddition.getItemStack();
            OreReference oreRef = OreHelper.INSTANCE.isOre(stack);
            if (oreRef != null) {
                Collection<String> oreIds = oreRef.getEquivalents();
                for (String oreId : oreIds) {
                    for (String prefix : ORE_PREFIXES) {
                        if (oreId.startsWith(prefix)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return true;
    }

    /**
     * 此物品是否可存储在存储单元中。
     *
     * @return 始终返回 false，因为这个物品本身是存储单元
     */
    @Override
    public boolean storableInStorageCell() {
        return false; // 不允许将存储单元存储在其他存储单元中
    }

    /**
     * 判断给定的物品堆栈是否是存储单元。
     *
     * @param i 物品堆栈
     * @return 如果是存储单元则返回 true，否则返回 false
     */
    @Override
    public boolean isStorageCell(ItemStack i) {
        return true; // 这个物品是存储单元
    }

    /**
     * 获取存储单元的闲置能量消耗。
     *
     * @return 闲置能量消耗值
     */
    @Override
    public double getIdleDrain() {
        return this.idleDrain; // 返回默认的闲置能量消耗
    }

    /**
     * 判断该物品是否可编辑。
     *
     * @param is 物品堆栈
     * @return 如果可编辑则返回 true，否则返回 false
     */
    @Override
    public boolean isEditable(ItemStack is) {
        return true; // 物品可以通过单元工作台进行编辑
    }

    /**
     * 获取升级物品栏。
     *
     * @param is 物品堆栈
     * @return 升级物品栏实例
     */
    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0); // 返回升级物品栏实例，但不支持升级（槽位为0）
    }

    /**
     * 获取配置物品栏。
     *
     * @param is 物品堆栈
     * @return 配置物品栏实例
     */
    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is); // 返回配置物品栏实例
    }

    /**
     * 获取物品的模糊模式。
     *
     * @param is 物品堆栈
     * @return 物品的模糊模式
     */
    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = CommonUtils.openNbtData(is)
            .getString("FuzzyMode");
        if (fz != null && !fz.isEmpty()) {
            return FuzzyMode.valueOf(fz);
        }
        return FuzzyMode.IGNORE_ALL; // 默认模糊模式
    }

    /**
     * 设置该物品的模糊模式。
     *
     * @param is     物品堆栈
     * @param fzMode 目标模糊模式
     */
    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        CommonUtils.openNbtData(is)
            .setString("FuzzyMode", fzMode.name());
    }

    /**
     * 获取矿物字典过滤器
     *
     * @param is 物品堆栈
     * @return 矿典过滤器字符串
     */
    @Override
    public String getOreFilter(ItemStack is) {
        return CommonUtils.openNbtData(is)
            .getString("OreFilter");
    }

    /**
     * 设置矿物字典过滤器
     *
     * @param is     存储单元物品
     * @param filter 矿典过滤器字符串
     */
    @Override
    public void setOreFilter(ItemStack is, String filter) {
        CommonUtils.openNbtData(is)
            .setString("OreFilter", filter);
    }

    /**
     * 获取库存管理实例
     *
     * @param o         物品堆栈
     * @param container 存储提供者，用于保存和管理数据
     * @return 库存管理实例
     * @throws AppEngException 初始化失败时抛出异常
     */
    @Override
    public OreStorageCellInventory getCellInv(ItemStack o, ISaveProvider container) throws AppEngException {
        return new OreStorageCellInventory(o, container);
    }

    @Override
    public void register() {
        GameRegistry.registerItem(this, NameConstants.ITEM_RAW_ORE_STORAGE_CELL);
    }

    /**
     * 获取矿石定义
     *
     * @return 矿石定义字符串
     */
    private String getOreDefs() {
        if (oreDefs == null) {
            oreDefs = String.join(", ", ORE_PREFIXES);
        }
        return oreDefs;
    }
}
