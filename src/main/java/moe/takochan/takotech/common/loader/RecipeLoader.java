package moe.takochan.takotech.common.loader;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import appeng.api.AEApi;
import appeng.core.ApiDefinitions;
import appeng.core.api.definitions.ApiMaterials;
import cpw.mods.fml.common.registry.GameRegistry;
import ic2.core.Ic2Items;
import moe.takochan.takotech.common.recipe.ToolboxPlusRecipe;

/**
 * 配方注册
 */
public class RecipeLoader implements Runnable {

    private static ItemStack BLOCK_COBBLESTONE;
    private static ItemStack BLOCK_IRON;

    private static ItemStack ITEM_AE2_EMPTY_STORAGE_CELL;
    private static ItemStack ITEM_AE2_CELL_64_PART;

    private static ItemStack ITEM_IC2_TOOLBOX;
    private static ItemStack ITEM_IRON_ORE;
    private static ItemStack ITEM_GOLD_ORE;
    private static ItemStack ITEM_IRON_INGOT;

    public RecipeLoader() {
        ApiDefinitions aeDef = (ApiDefinitions) AEApi.instance()
            .definitions();
        ApiMaterials aeMaterials = aeDef.materials();

        BLOCK_COBBLESTONE = new ItemStack(Blocks.cobblestone);
        BLOCK_IRON = new ItemStack(Blocks.iron_block);

        ITEM_AE2_EMPTY_STORAGE_CELL = aeMaterials.emptyStorageCell()
            .maybeStack(1)
            .orNull();

        ITEM_AE2_CELL_64_PART = aeMaterials.cell64kPart()
            .maybeStack(1)
            .orNull();

        ITEM_IC2_TOOLBOX = Ic2Items.toolbox;

        // 加载一些备用物品，以防矿物词典不可用
        ITEM_IRON_ORE = new ItemStack(Blocks.iron_ore);
        ITEM_GOLD_ORE = new ItemStack(Blocks.gold_ore);
        ITEM_IRON_INGOT = new ItemStack(Items.iron_ingot);
    }

    @Override
    public void run() {
        registryRecipe();
    }

    private void registryRecipe() {
        // 添加主合成表
        addMainRecipes();

        // 添加备用合成表，以防矿物词典物品不可用
        addFallbackRecipes();

        // 工具箱升级配方
        GameRegistry.addRecipe(new ToolboxPlusRecipe(new ItemStack(ItemLoader.ITEM_TOOLBOX_PLUS, 1), ITEM_IC2_TOOLBOX));
    }

    /**
     * 添加主要合成表，使用矿物词典
     */
    private void addMainRecipes() {
        // 1. 矿石存储元件 - 使用矿石辞典中的铁矿石
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(ItemLoader.ITEM_ORE_STORAGE_CELL, 1),
                "CDC",
                "DED",
                "CDC",
                'C',
                ITEM_AE2_EMPTY_STORAGE_CELL,
                'D',
                "oreIron", // 使用矿物词典的铁矿石
                'E',
                ITEM_AE2_CELL_64_PART));

        // 2. 粗矿存储元件 - 使用GT5U的粗铁矿
        try {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ItemLoader.ITEM_RAW_ORE_STORAGE_CELL, 1),
                    "CDC",
                    "DED",
                    "CDC",
                    'C',
                    ITEM_AE2_EMPTY_STORAGE_CELL,
                    'D',
                    "rawOreIron", // 使用GT5U的粗铁矿
                    'E',
                    ITEM_AE2_CELL_64_PART));
        } catch (Exception e) {
            // 如果添加失败，将在addFallbackRecipes中添加备用合成表
        }

        // 3. 粉碎矿物存储元件 - 使用GT5U/IC2的粉碎铁矿
        try {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ItemLoader.ITEM_CRUSHED_ORE_STORAGE_CELL, 1),
                    "CDC",
                    "DED",
                    "CDC",
                    'C',
                    ITEM_AE2_EMPTY_STORAGE_CELL,
                    'D',
                    "crushedIron", // 使用粉碎铁矿
                    'E',
                    ITEM_AE2_CELL_64_PART));
        } catch (Exception e) {
            // 如果添加失败，将在addFallbackRecipes中添加备用合成表
        }

        // 4. 粉末存储元件 - 使用矿物词典中的铁粉
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(ItemLoader.ITEM_DUST_STORAGE_CELL, 1),
                "CDC",
                "DED",
                "CDC",
                'C',
                ITEM_AE2_EMPTY_STORAGE_CELL,
                'D',
                "dustIron", // 使用铁粉
                'E',
                ITEM_AE2_CELL_64_PART));
    }

    /**
     * 添加备用合成表，使用原版物品
     */
    private void addFallbackRecipes() {
        // 2. 粗矿存储元件备用合成表 - 使用铁块
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(ItemLoader.ITEM_RAW_ORE_STORAGE_CELL, 1),
                "CDC",
                "DED",
                "CDC",
                'C',
                ITEM_AE2_EMPTY_STORAGE_CELL,
                'D',
                "blockIron", // 使用铁块作为备用
                'E',
                ITEM_AE2_CELL_64_PART));

        // 3. 粉碎矿物存储元件备用合成表 - 使用金矿石
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(ItemLoader.ITEM_CRUSHED_ORE_STORAGE_CELL, 1),
                "CDC",
                "DED",
                "CDC",
                'C',
                ITEM_AE2_EMPTY_STORAGE_CELL,
                'D',
                "oreGold", // 使用金矿石作为备用
                'E',
                ITEM_AE2_CELL_64_PART));

        // 4. 粉末存储元件备用合成表 - 使用铁锭
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(ItemLoader.ITEM_DUST_STORAGE_CELL, 1),
                "CDC",
                "DED",
                "CDC",
                'C',
                ITEM_AE2_EMPTY_STORAGE_CELL,
                'D',
                "ingotIron", // 使用铁锭作为备用
                'E',
                ITEM_AE2_CELL_64_PART));
    }
}
