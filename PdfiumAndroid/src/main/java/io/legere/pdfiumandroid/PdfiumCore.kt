@file:Suppress("unused")

package io.legere.pdfiumandroid

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field

/**
 * PdfiumCore is the main entry-point for access to the PDFium API.
 */
class PdfiumCore {

    private external fun nativeOpenDocument(fd: Int, password: String?): Long
    private external fun nativeOpenMemDocument(data: ByteArray?, password: String?): Long

    private external fun nativeGetLinkRect(linkPtr: Long): RectF?

    /** Context needed to get screen density  */
    init {
        Log.d(TAG, "Starting PdfiumAndroid ")
    }

    /**
     * Create new document from file
     * @param fd opened file descriptor of file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocument {
        return newDocument(fd, null)
    }

    /**
     * Create new document from file with password
     * @param fd opened file descriptor of file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenDocument(getNumFd(fd), password)).also { document ->
                document.parcelFileDescriptor = fd
            }
        }
    }

    /**
     * Create new document from bytearray
     * @param data bytearray of pdf file
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?): PdfDocument {
        return newDocument(data, null)
    }

    /**
     * Create new document from bytearray with password
     * @param data bytearray of pdf file
     * @param password password for decryption
     * @return PdfDocument
     */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray?, password: String?): PdfDocument {
        synchronized(lock) {
            return PdfDocument(nativeOpenMemDocument(data, password)).also { document ->
                document.parcelFileDescriptor = null
            }
        }
    }

    companion object {
        private val TAG = PdfiumCore::class.java.name
        private val FD_CLASS: Class<*> = FileDescriptor::class.java
        private const val FD_FIELD_NAME = "descriptor"

        init {
            try {
                System.loadLibrary("partition_alloc.cr")
                System.loadLibrary("c++_chrome.cr")
                System.loadLibrary("chrome_zlib.cr")
                System.loadLibrary("absl.cr")
                System.loadLibrary("icuuc.cr")
                System.loadLibrary("pdfium.cr")
                System.loadLibrary("pdfiumandroid")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native libraries failed to load - $e")
            }
        }

        /* synchronize native methods */
        val lock = Any()
        private var mFdField: Field? = null
        fun getNumFd(fdObj: ParcelFileDescriptor): Int {
            return try {
                if (mFdField == null) {
                    mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME)
                    mFdField?.isAccessible = true
                }
                mFdField?.getInt(fdObj.fileDescriptor) ?: -1
            } catch (e: NoSuchFieldException) {
                Log.e(TAG, "getFdField NoSuchFieldException", e)
                -1
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "IllegalAccessException", e)
                -1
            }
        }
    }
}
