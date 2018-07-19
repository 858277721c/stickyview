# About

# Gradle
[![](https://jitpack.io/v/zj565061763/stickyview.svg)](https://jitpack.io/#zj565061763/stickyview)

# 简单效果
![](https://raw.githubusercontent.com/zj565061763/stickyview/master/screenshot/stickyview.gif)

# 简单使用
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.fanwe.lib.stickyview.FStickyLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/sticky_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:autoFind="true">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <View
                android:layout_width="match_parent"
                android:layout_height="50dp"
                android:background="@color/colorAccent" />

            <!-- 包裹要Sticky的内容 -->
            <com.fanwe.lib.stickyview.FStickyWrapper
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <Button
                    android:layout_width="match_parent"
                    android:layout_height="100dp"
                    android:text="sticky" />

            </com.fanwe.lib.stickyview.FStickyWrapper>

            <View
                android:layout_width="match_parent"
                android:layout_height="2000dp"
                android:background="@color/colorAccent" />

        </LinearLayout>
    </ScrollView>
</com.fanwe.lib.stickyview.FStickyLayout>
```
假如设置了该属性，则会自动查找要Sticky的布局<br>
```xml
app:autoFind="true"
```
FStickyLayout常用的方法：
```java
/**
 * 设置调试模式，日志tag:{@link FStickyContainer#getDebugTag()}
 *
 * @param debug
 */
public void setDebug(boolean debug);

/**
 * 添加Sticky
 *
 * @param wrapper
 */
public void addSticky(FStickyWrapper wrapper);

/**
 * 移除Sticky
 *
 * @param wrapper
 */
public void removeSticky(FStickyWrapper wrapper);

/**
 * 添加当前对象下的所有Sticky
 */
public void findAllSticky();
```