package moe.takochan.takotech.common.loader;

import moe.takochan.takotech.common.item.ae.ItemCrushedOreStorageCell;
import moe.takochan.takotech.common.item.ae.ItemDustStorageCell;
import moe.takochan.takotech.common.item.ae.ItemOreStorageCell;
import moe.takochan.takotech.common.item.ae.ItemRawOreStorageCell;
import moe.takochan.takotech.common.item.ic2.ItemToolboxPlus;

/**
 * 物品注册
 */
public class ItemLoader implements Runnable {

    // 矿物存储元件
    public static ItemOreStorageCell ITEM_ORE_STORAGE_CELL;
    public static ItemRawOreStorageCell ITEM_RAW_ORE_STORAGE_CELL;
    public static ItemCrushedOreStorageCell ITEM_CRUSHED_ORE_STORAGE_CELL;
    public static ItemDustStorageCell ITEM_DUST_STORAGE_CELL;

    public static ItemToolboxPlus ITEM_TOOLBOX_PLUS;

    public ItemLoader() {
        // 初始化矿物存储元件
        ITEM_ORE_STORAGE_CELL = new ItemOreStorageCell();
        ITEM_RAW_ORE_STORAGE_CELL = new ItemRawOreStorageCell();
        ITEM_CRUSHED_ORE_STORAGE_CELL = new ItemCrushedOreStorageCell();
        ITEM_DUST_STORAGE_CELL = new ItemDustStorageCell();

        ITEM_TOOLBOX_PLUS = new ItemToolboxPlus();
    }

    @Override
    public void run() {
        registerItems();
    }

    private void registerItems() {
        // 注册矿物存储元件
        ITEM_ORE_STORAGE_CELL.register();
        ITEM_RAW_ORE_STORAGE_CELL.register();
        ITEM_CRUSHED_ORE_STORAGE_CELL.register();
        ITEM_DUST_STORAGE_CELL.register();

        ITEM_TOOLBOX_PLUS.register();
    }
}
