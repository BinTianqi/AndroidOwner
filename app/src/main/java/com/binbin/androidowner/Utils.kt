package com.binbin.androidowner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


fun uriToStream(
    context: Context,
    uri: Uri?,
    operation:(stream: InputStream)->Unit
){
    if(uri!=null){
        try{
            val stream = context.contentResolver.openInputStream(uri)
            if(stream!=null) { operation(stream) }
            else{ Toast.makeText(context, "空的流", Toast.LENGTH_SHORT).show() }
            stream?.close()
        }
        catch(e: FileNotFoundException){ Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show() }
        catch(e: IOException){ Toast.makeText(context, "IO异常", Toast.LENGTH_SHORT).show() }
    }else{ Toast.makeText(context, "空URI", Toast.LENGTH_SHORT).show() }
}

fun List<Any>.toText():String{
    var output = ""
    var isFirst = true
    for(each in listIterator()){
        if(isFirst){isFirst=false}else{output+="\n"}
        output+=each
    }
    return output
}

fun Set<Any>.toText():String{
    var output = ""
    var isFirst = true
    for(each in iterator()){
        if(isFirst){isFirst=false}else{output+="\n"}
        output+=each
    }
    return output
}

fun writeClipBoard(context: Context, string: String):Boolean{
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        if(VERSION.SDK_INT>=23){
            val hasPermission: Boolean = clipboardManager.hasPrimaryClip()
            if(!hasPermission) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.setData(Uri.parse("package:"+context.packageName))
                startActivity(context,intent,null)
            }
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", string))
    }catch(e:Exception){
        return false
    }
    return true
}
