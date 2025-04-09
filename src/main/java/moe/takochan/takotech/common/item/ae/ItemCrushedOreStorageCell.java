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
 * 粉碎矿物存储元件
 * <p>
 * 只接受带有"crushed"矿典前缀的物品（粉碎、洗净、离心矿物）
 */
public class ItemCrushedOreStorageCell extends BaseAECellItem implements IStorageCell, IItemGroup {

    private static final String[] ORE_PREFIXES = { "crushed" };
    private static String oreDefs;

    private final int perType = 1;
    private final double idleDrain;

    @SuppressWarnings("Guava")
    public ItemCrushedOreStorageCell() {
        super(NameConstants.ITEM_CRUSHED_ORE_STORAGE_CELL);

        idleDrain = 1.14;

        this.setMaxStackSize(1);
        this.setTextureName(CommonUtils.resource(NameConstants.ITEM_CRUSHED_ORE_STORAGE_CELL));
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
    }

    // 其他方法与ItemRawOreStorageCell基本相同，只需修改描述和过滤前缀
    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> lines,
        final boolean displayMoreInfo) {
        lines.add(I18nUtils.tooltip(NameConstants.ITEM_CRUSHED_ORE_STORAGE_CELL_DESC));
        lines.add("§a仅存储粉碎矿物 (crushed)"); // 添加专用描述

        // ... 与ItemRawOreStorageCell相同 ...
        final IMEInventoryHandler<?> inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(itemStack, null, StorageChannel.ITEMS);

        if (inventory instanceof ITakoCellInventoryHandler handler) {
            final ITakoCellInventory cellInventory = handler.getCellInv();

            if (cellInventory != null) {
                if (!cellInventory.getDiskID()
                    .isEmpty()) {
                    lines.add(cellInventory.getDiskID());
                }

                lines.add(
                    NumberFormat.getInstance()
                        .format(cellInventory.getStoredItemTypes()) + " "
                        + GuiText.Of.getLocal()
                        + ' '
                        + NumberFormat.getInstance()
                            .format(this.getTotalTypes(itemStack))
                        + ' '
                        + GuiText.Types.getLocal());

                if (handler.isPreformatted()) {
                    String filter = cellInventory.getOreFilter();
                    if (filter.isEmpty()) {
                        final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST
                            ? GuiText.Included
                            : GuiText.Excluded).getLocal();

                        if (handler.isFuzzy()) {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
                        } else {
                            lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                        }

                        if (GuiScreen.isShiftKeyDown()) {
                            lines.add(GuiText.Filter.getLocal() + ": ");
                            for (int i = 0; i < cellInventory.getConfigInventory()
                                .getSizeInventory(); ++i) {
                                ItemStack s = cellInventory.getConfigInventory()
                                    .getStackInSlot(i);
                                if (s != null) {
                                    lines.add(s.getDisplayName());
                                }
                            }
                        }
                    } else {
                        lines.add(GuiText.PartitionedOre.getLocal() + " : " + filter);
                    }
                }

                String defs = getOreDefs();
                if (defs != null && !defs.isEmpty()) {
                    lines.add(I18nUtils.tooltip(NameConstants.ITEM_CRUSHED_ORE_STORAGE_CELL_DESC) + ".1: ");
                    lines.add(defs);
                }
            }
        }

        super.addCheckedInformation(itemStack, player, lines, displayMoreInfo);
    }

    // ... 其他标准方法与ItemRawOreStorageCell相同 ...
    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        if (requestedAddition != null) {
            ItemStack stack = requestedAddition.getItemStack();
            OreReference oreRef = OreHelper.INSTANCE.isOre(stack);
            if (oreRef != null) {
                // Get all ore dictionary names associated with this item
                Collection<String> oreIds = oreRef.getEquivalents();
                // Check if any ore dictionary name starts with our prefixes
                for (String oreId : oreIds) {
                    for (String prefix : ORE_PREFIXES) {
                        if (oreId.startsWith(prefix)) {
                            return false; // Contains specified prefix, allow storage
                        }
                    }
                }
            }
            return true; // No matching ore dictionary, disallow storage
        }
        return true; // No requested addition, disallow storage by default
    }

    @Override
    public void register() {
        GameRegistry.registerItem(this, NameConstants.ITEM_CRUSHED_ORE_STORAGE_CELL);
    }

    private String getOreDefs() {
        if (oreDefs == null) {
            oreDefs = String.join(", ", ORE_PREFIXES);
        }
        return oreDefs;
    }

    // ... 其他必要方法 ...
    @Override
    public int getBytes(ItemStack cellItem) {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getBytesLong(final ItemStack cellItem) {
        return Long.MAX_VALUE;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 63;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = CommonUtils.openNbtData(is)
            .getString("FuzzyMode");
        if (fz != null && !fz.isEmpty()) {
            return FuzzyMode.valueOf(fz);
        }
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        CommonUtils.openNbtData(is)
            .setString("FuzzyMode", fzMode.name());
    }

    @Override
    public String getOreFilter(ItemStack is) {
        return CommonUtils.openNbtData(is)
            .getString("OreFilter");
    }

    @Override
    public void setOreFilter(ItemStack is, String filter) {
        CommonUtils.openNbtData(is)
            .setString("OreFilter", filter);
    }

    @Override
    public OreStorageCellInventory getCellInv(ItemStack o, ISaveProvider container) throws AppEngException {
        return new OreStorageCellInventory(o, container);
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
}
