//
// Created by gongguopei on 2019/5/31.
//

//
// Created by wangjiang on 2019/4/8.
//

#include <jni.h>
#include <unistd.h>
#include <sys/stat.h>
#include <ctime>
#include <cstdlib>
#include <fcntl.h>

#include <string>
#include <deque>
#include <cstdlib>
#include <cstring>
// #include "filter_16_v2.cpp"
#include "streamswtqua.h"
#include "commalgorithm.h"
#include "swt.h"
#include <android/log.h>
#include <cassert>

extern "C"
JNIEXPORT jshortArray JNICALL
Java_com_viatom_er2_activity_MainActivity_00024Companion_shortFilter(JNIEnv *env, jobject thiz,
                                                                     jshortArray inShorts) {

    short *shortArray;
    jsize arraySize;
    arraySize = (*env).GetArrayLength(inShorts);

    deque<double> inputt;
    inputt.clear();

    auto *isCopy = (jboolean *) malloc(sizeof(jboolean));
    shortArray = (*env).GetShortArrayElements(inShorts, isCopy);
    for (int j = 0; j < arraySize; j++) {
        inputt.push_back((jdouble) shortArray[j]);
    }
    int inputLength = (int) inputt.size();

    int i;
    StreamSwtQua streamSwtQua;
    deque<double> outputPoints;
    deque<double> allSig;
    deque<double> outputsize;
    int ReduntLength;
    int MultipleSize;
    MultipleSize = inputLength / 256;
    ReduntLength = inputLength - 256 * MultipleSize;

    if (ReduntLength != 0) {
        for (i = inputLength; i < (MultipleSize + 1) * 256; i++) {
            inputt.push_back(0);
        }
    }


    if (ReduntLength == 0) {
        for (i = 0; i < 256 * MultipleSize; ++i) {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (double &outputPoint : outputPoints) {
                allSig.push_back(outputPoint);
            }
        }

    } else {
        for (i = 0; i < inputt.size(); i++) {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (double &outputPoint : outputPoints) {
                allSig.push_back(outputPoint);
            }
        }
        if (ReduntLength < 192) {
            for (i = 0; i < 192 - ReduntLength; i++) {
                allSig.pop_back();
            }
        }
    }

    long length = allSig.size();

    short array[length];
    for (i = 0; i < length; i++) {
        array[i] = (short) allSig[i];
    }

    auto size = (jsize) allSig.size();
    jshortArray result = (*env).NewShortArray(size);
    (*env).SetShortArrayRegion(result, 0, size, array);

    return result;
}

