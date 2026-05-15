package com.lotlimys.yeseatwhat.data.db.seed;

import com.lotlimys.yeseatwhat.data.db.dao.CategoryDao;
import com.lotlimys.yeseatwhat.data.db.dao.IngredientDao;
import com.lotlimys.yeseatwhat.data.db.entity.Category;
import com.lotlimys.yeseatwhat.data.db.entity.SystemIngredient;

public class DataSeeder {

    public static void seedCategories(CategoryDao dao) {
        Category[] categories = {
                new Category(1, "肉类", "apic", "SYSTEM", 1),
                new Category(2, "蔬菜", "apic", "SYSTEM", 2),
                new Category(3, "水果", "apic", "SYSTEM", 3),
                new Category(4, "海鲜", "apic", "SYSTEM", 4),
                new Category(5, "调味料", "apic", "SYSTEM", 5),
                new Category(6, "主食", "apic", "SYSTEM", 6),
                new Category(7, "豆制品/蛋奶", "apic", "SYSTEM", 7),
                new Category(8, "干货/其它", "apic", "SYSTEM", 8)
        };
        dao.insert(categories);
    }

    public static void seedIngredients(IngredientDao dao) {
        SystemIngredient[] ingredients = {
                // 肉类 (categoryId=1)
                new SystemIngredient(1, "猪五花肉", "apic", 1, 1),
                new SystemIngredient(2, "猪里脊", "apic", 1, 2),
                new SystemIngredient(3, "猪排骨", "apic", 1, 3),
                new SystemIngredient(4, "鸡胸肉", "apic", 1, 4),
                new SystemIngredient(5, "鸡腿肉", "apic", 1, 5),
                new SystemIngredient(6, "鸡翅", "apic", 1, 6),
                new SystemIngredient(7, "牛腩", "apic", 1, 7),
                new SystemIngredient(8, "牛里脊", "apic", 1, 8),
                new SystemIngredient(9, "羊肉", "apic", 1, 9),
                new SystemIngredient(10, "鸭肉", "apic", 1, 10),

                // 蔬菜 (categoryId=2)
                new SystemIngredient(11, "白菜", "apic", 2, 1),
                new SystemIngredient(12, "菠菜", "apic", 2, 2),
                new SystemIngredient(13, "西兰花", "apic", 2, 3),
                new SystemIngredient(14, "菜花", "apic", 2, 4),
                new SystemIngredient(15, "生菜", "apic", 2, 5),
                new SystemIngredient(16, "西红柿", "apic", 2, 6),
                new SystemIngredient(17, "黄瓜", "apic", 2, 7),
                new SystemIngredient(18, "土豆", "apic", 2, 8),
                new SystemIngredient(19, "胡萝卜", "apic", 2, 9),
                new SystemIngredient(20, "洋葱", "apic", 2, 10),
                new SystemIngredient(21, "青椒", "apic", 2, 11),
                new SystemIngredient(22, "茄子", "apic", 2, 12),
                new SystemIngredient(23, "玉米", "apic", 2, 13),
                new SystemIngredient(24, "蘑菇", "apic", 2, 14),
                new SystemIngredient(25, "蒜苗", "apic", 2, 15),
                new SystemIngredient(26, "豆角", "apic", 2, 16),

                // 水果 (categoryId=3)
                new SystemIngredient(27, "苹果", "apic", 3, 1),
                new SystemIngredient(28, "香蕉", "apic", 3, 2),
                new SystemIngredient(29, "橙子", "apic", 3, 3),
                new SystemIngredient(30, "柠檬", "apic", 3, 4),
                new SystemIngredient(31, "草莓", "apic", 3, 5),
                new SystemIngredient(32, "葡萄", "apic", 3, 6),
                new SystemIngredient(33, "芒果", "apic", 3, 7),
                new SystemIngredient(34, "菠萝", "apic", 3, 8),
                new SystemIngredient(35, "西瓜", "apic", 3, 9),
                new SystemIngredient(36, "蓝莓", "apic", 3, 10),

                // 海鲜 (categoryId=4)
                new SystemIngredient(37, "虾", "apic", 4, 1),
                new SystemIngredient(38, "鱼", "apic", 4, 2),
                new SystemIngredient(39, "螃蟹", "apic", 4, 3),
                new SystemIngredient(40, "蛤蜊", "apic", 4, 4),
                new SystemIngredient(41, "鱿鱼", "apic", 4, 5),
                new SystemIngredient(42, "扇贝", "apic", 4, 6),

                // 调味料 (categoryId=5)
                new SystemIngredient(43, "酱油", "apic", 5, 1),
                new SystemIngredient(44, "醋", "apic", 5, 2),
                new SystemIngredient(45, "料酒", "apic", 5, 3),
                new SystemIngredient(46, "食用油", "apic", 5, 4),
                new SystemIngredient(47, "盐", "apic", 5, 5),
                new SystemIngredient(48, "糖", "apic", 5, 6),
                new SystemIngredient(49, "生抽", "apic", 5, 7),
                new SystemIngredient(50, "老抽", "apic", 5, 8),
                new SystemIngredient(51, "蚝油", "apic", 5, 9),
                new SystemIngredient(52, "豆瓣酱", "apic", 5, 10),
                new SystemIngredient(53, "辣椒", "apic", 5, 11),
                new SystemIngredient(54, "花椒", "apic", 5, 12),
                new SystemIngredient(55, "姜", "apic", 5, 13),
                new SystemIngredient(56, "蒜", "apic", 5, 14),
                new SystemIngredient(57, "葱", "apic", 5, 15),

                // 主食 (categoryId=6)
                new SystemIngredient(58, "大米", "apic", 6, 1),
                new SystemIngredient(59, "面条", "apic", 6, 2),
                new SystemIngredient(60, "面粉", "apic", 6, 3),
                new SystemIngredient(61, "饺子皮", "apic", 6, 4),
                new SystemIngredient(62, "糯米", "apic", 6, 5),
                new SystemIngredient(63, "馒头", "apic", 6, 6),
                new SystemIngredient(64, "小米", "apic", 6, 7),

                // 豆制品/蛋奶 (categoryId=7)
                new SystemIngredient(65, "豆腐", "apic", 7, 1),
                new SystemIngredient(66, "鸡蛋", "apic", 7, 2),
                new SystemIngredient(67, "牛奶", "apic", 7, 3),
                new SystemIngredient(68, "豆浆", "apic", 7, 4),
                new SystemIngredient(69, "酸奶", "apic", 7, 5),
                new SystemIngredient(70, "芝士", "apic", 7, 6),
                new SystemIngredient(71, "腐竹", "apic", 7, 7),

                // 干货/其它 (categoryId=8)
                new SystemIngredient(72, "花生", "apic", 8, 1),
                new SystemIngredient(73, "核桃", "apic", 8, 2),
                new SystemIngredient(74, "红枣", "apic", 8, 3),
                new SystemIngredient(75, "枸杞", "apic", 8, 4),
                new SystemIngredient(76, "木耳", "apic", 8, 5),
                new SystemIngredient(77, "香菇", "apic", 8, 6),
                new SystemIngredient(78, "紫菜", "apic", 8, 7),
                new SystemIngredient(79, "海带", "apic", 8, 8),
                new SystemIngredient(80, "桂圆", "apic", 8, 9),
        };
        dao.insertSystem(ingredients);
    }
}
