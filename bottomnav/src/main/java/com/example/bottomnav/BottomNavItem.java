package com.example.bottomnav;
/**
 * 底部导航的“数据模型”：只负责存标题和图标资源。
 * 新手理解：它就是一张“菜单卡片”的描述。
 */
public class BottomNavItem {
    /** 导航标题（比如“首页”） */
    private final String title;
    /** 图标资源 id（0 表示不显示图标） */
    private final int iconResId;

    /** 只有标题的构造方法 */
    public BottomNavItem(String title) {
        this(title, 0);
    }

    /** 标题 + 图标的构造方法 */
    public BottomNavItem(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    /** 获取标题 */
    public String getTitle() {
        return title;
    }

    /** 获取图标资源 id */
    public int getIconResId() {
        return iconResId;
    }
}
