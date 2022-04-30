package com.se.themusicplayer

import java.lang.StringBuilder

/** 毫秒转格式化时间 */
fun Int.formatToTime():CharSequence = if(this/1000 == 0) "00:00" else {
    var msBuf = this/1000
    val time = StringBuilder()

    if(msBuf/60 == 0){
        time.insert(0,msBuf)
        if (msBuf<10) time.insert(0,"0")
        time.insert(0,"00:")
    } else {
        while (msBuf != 0) {
            time.insert(0, msBuf % 60)
            if (msBuf / 60 != 0) {
                if (msBuf % 60 < 10) time.insert(0, "0")
                time.insert(0, ":")
            }
            msBuf /= 60
        }
        time
    }
}
