package com.binbin.androidowner.dpm

import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.*
import android.os.Build.VERSION
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "已启用", Toast.LENGTH_SHORT).show()
    }
    
    override fun onTransferOwnershipComplete(context: Context, bundle: PersistableBundle?) {
        super.onTransferOwnershipComplete(context, bundle)
        if(bundle!=null){
            Toast.makeText(context,"转移所有权完毕，附加内容长度：${bundle.size()}",Toast.LENGTH_SHORT).show()
            Log.d("TransferOwnerShip",bundle.toString())
        }else{
            Toast.makeText(context,"转移所有权完毕，无附加内容}",Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Toast.makeText(context, "创建工作资料完成", Toast.LENGTH_SHORT).show()
    }
    
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }
    
    override fun onNetworkLogsAvailable(context: Context, intent: Intent, batchToken: Long, networkLogsCount: Int) {
        super.onNetworkLogsAvailable(context, intent, batchToken, networkLogsCount)
        Toast.makeText(context,"可以收集网络日志",Toast.LENGTH_SHORT).show()
        Log.e("","网络日志可用")
    }
    
    override fun onSecurityLogsAvailable(context: Context, intent: Intent) {
        super.onSecurityLogsAvailable(context, intent)
        Toast.makeText(context,"可以收集安全日志",Toast.LENGTH_SHORT).show()
        Log.e("","安全日志可用")
    }
    
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Toast.makeText(context, "撤销授权", Toast.LENGTH_SHORT).show()
        return "这是取消时的提示"
    }
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "已禁用", Toast.LENGTH_SHORT).show()
    }
    override fun onSystemUpdatePending(context: Context, intent: Intent, receivedTime: Long) {
        if (VERSION.SDK_INT < 26) { return }
        Toast.makeText(context, "新的系统更新！", Toast.LENGTH_SHORT).show()
    }
}

class PackageInstallerReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val toastText = when(intent.getIntExtra(EXTRA_STATUS,666)){
            STATUS_PENDING_USER_ACTION->"等待用户交互"
            STATUS_SUCCESS->"成功"
            STATUS_FAILURE->"失败"
            STATUS_FAILURE_BLOCKED->"失败：被阻止"
            STATUS_FAILURE_ABORTED->"失败：被打断"
            STATUS_FAILURE_INVALID->"失败：无效"
            STATUS_FAILURE_CONFLICT->"失败：冲突"
            STATUS_FAILURE_STORAGE->"失败：空间不足"
            STATUS_FAILURE_INCOMPATIBLE->"失败：不兼容"
            STATUS_FAILURE_TIMEOUT->"失败：超时"
            else->"未知"
        }
        Log.e("静默安装","${intent.getIntExtra(EXTRA_STATUS,666)}:$toastText")
    }
}
