# PdfiumAndroidKt

Current version: 1.0.21

A Pdfium Android library using the latest stable version Pdfium.  Written in Kotlin with coroutines to easily talk to the native code off the main thread.

Based on https://github.com/barteksc/PdfiumAndroid and https://github.com/johngray1965/PdfiumAndroidKt

It offers PDFView which can display documents. This view supports password encrypted pdfs. In case of an invalid password PdfPasswordException is thrown.

To use it, build aar and include it in the app.
