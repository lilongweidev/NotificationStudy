package com.llw.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.llw.notification.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //渠道Id
    private val channelId = "test"

    //渠道名
    private val channelName = "测试通知"

    //渠道重要级
    private val importance = NotificationManagerCompat.IMPORTANCE_HIGH

    //通知管理者
    private lateinit var notificationManager: NotificationManager

    //通知
    private lateinit var notification: Notification

    //回复通知
    private lateinit var replyNotification: Notification

    //横幅通知
    private lateinit var bannerNotification: Notification

    //通知Id
    private val notificationId = 1

    //回复通知Id
    private val replyNotificationId = 2

    //横幅通知Id
    private val bannerNotificationId = 3

    //开启横幅通知返回
    private val bannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Log.d("TAG", "返回结果")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //获取系统通知服务
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //初始化通知
        initNotification()
        initReplyNotification()
        initBannerNotification()
        //显示通知
        binding.btnShow.setOnClickListener {
            notificationManager.notify(notificationId, notification)
        }
        //显示回复通知
        binding.btnShowReply.setOnClickListener {
            notificationManager.notify(replyNotificationId, replyNotification)
        }
        //显示横幅通知
        binding.btnShowBanner.setOnClickListener {
            //检查是否授予访问权限
            if (openBannerNotification()) {
                notificationManager.notify(bannerNotificationId, bannerNotification)
            }
        }
    }

    /**
     * 是否开启横幅通知
     */
    private fun openBannerNotification() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val bannerImportance = notificationManager.getNotificationChannel("banner").importance
        if (bannerImportance == NotificationManager.IMPORTANCE_DEFAULT) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, "banner")
            bannerLauncher.launch(intent); false
        } else true
    } else true

    /**
     * 初始化通知
     */
    private fun initNotification() {
        val title = "打工人"
        val content = "我要搞钱！！！富强、明主、文明、和谐、自由、平等、公正、法治、爱国、敬业、诚信、友善"
        // 为DetailsActivity 创建显式 Intent
        val intent = Intent(this, DetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title).putExtra("content", content)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建通知渠道
            createNotificationChannel(channelId, channelName, importance)
            NotificationCompat.Builder(this, channelId)
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle(title)//标题
            setContentText(content)//内容
            setContentIntent(pendingIntent)//设置内容意图
            setAutoCancel(true)//设置自动取消
            setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(resources, R.drawable.logo))
            )//设置样式
        }.build()
    }

    /**
     * 初始化回复通知
     */
    private fun initReplyNotification() {
        //远程输入
        val remoteInput = RemoteInput.Builder("key_text_reply").setLabel("快速回复").build()
        //构建回复pendingIntent
        val replyIntent = Intent(this, ReplyMessageReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT)
        //点击通知的发送按钮
        val action =
            NotificationCompat.Action.Builder(0, "回复", pendingIntent).addRemoteInput(remoteInput)
                .build()
        //构建通知
        replyNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("reply", "回复消息", importance)
            NotificationCompat.Builder(this, "reply")
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle("1008666")//标题
            setContentText("你的账号已欠费2000元！")//内容
            addAction(action)
        }.build()
    }

    /**
     * 初始化横幅通知
     */
    private fun initBannerNotification() {
        //构建通知
        bannerNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createBannerNotificationChannel("banner", "提醒消息", importance)
            NotificationCompat.Builder(this, "banner")
        } else {
            NotificationCompat.Builder(this)
        }.apply {
            setSmallIcon(R.mipmap.ic_launcher)//小图标（显示在状态栏）
            setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))//大图标（显示在通知上）
            setContentTitle("落魄Android在线炒粉")//标题
            setContentText("不要9块9，不要6块9，只要3块9。")//内容
            setWhen(System.currentTimeMillis())//通知显示时间
            setAutoCancel(true)//设置自动取消
        }.build()
    }

    /**
     * 创建通知渠道
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) =
        notificationManager.createNotificationChannel(
            NotificationChannel(channelId, channelName, importance)
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBannerNotificationChannel(channelId: String, channelName: String, importance: Int) =
        notificationManager.createNotificationChannel(
            NotificationChannel(channelId, channelName, importance).apply {
                description = "提醒式通知"//渠道描述
                enableLights(true)//开启闪光灯
                lightColor = Color.BLUE//设置闪光灯颜色
                enableVibration(true)//开启震动
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)//震动模式
                setSound(null, null)//没有提示音
            }
        )
}