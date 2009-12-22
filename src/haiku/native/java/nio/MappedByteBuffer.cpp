/*
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
#include "java_nio_MappedByteBuffer.h"
#include <stdlib.h>

JNIEXPORT jboolean JNICALL 
Java_java_nio_MappedByteBuffer_isLoaded0(JNIEnv *env, jobject obj,
                                        jlong address, jlong len)
{
	fprintf(stderr, "%s: unimplemented", __func__);
    return false;
}

JNIEXPORT jint JNICALL
Java_java_nio_MappedByteBuffer_load0(JNIEnv *env, jobject obj, jlong address,
                                     jlong len, jint pageSize)
{
    int *ptr = (int *) jlong_to_ptr(address);
    int pageIncrement = pageSize / sizeof(int);
    jlong numPages = (len + pageSize - 1) / pageSize;
    int i = 0;
    int j = 0;

    /* touch every page */
    for (i=0; i<numPages; i++) {
        j += *((volatile int *)ptr);
        ptr += pageIncrement;
    }
    return j;
}

JNIEXPORT void JNICALL
Java_java_nio_MappedByteBuffer_force0(JNIEnv *env, jobject obj, jlong address,
                                      jlong len)
{
	fprintf(stderr, "%s: unimplemented", __func__);
}
