package com.dormiq.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dormiq.domain.model.RentPayment
import com.dormiq.domain.repository.HostelRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PdfReceiptEntryPoint {
    fun hostelRepository(): HostelRepository
}

object PdfReceiptGenerator {
    fun generateAndShareReceipt(context: Context, payment: RentPayment) {
        val appContext = context.applicationContext
        Toast.makeText(context, "Fetching hostel details & generating receipt...", Toast.LENGTH_SHORT).show()
        
        CoroutineScope(Dispatchers.Main).launch {
            val (hostelName, hostelAddress) = withContext(Dispatchers.IO) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        appContext,
                        PdfReceiptEntryPoint::class.java
                    )
                    val hostel = entryPoint.hostelRepository().getHostel(payment.hostelId)
                    if (hostel != null) {
                        Pair(hostel.name, hostel.address)
                    } else {
                        Pair("Dormiq Premium Hostel", "Premium Living Accommodation")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Pair("Dormiq Premium Hostel", "Premium Living Accommodation")
                }
            }
            
            drawPdf(context, payment, hostelName, hostelAddress)
        }
    }

    private fun drawPdf(context: Context, payment: RentPayment, hostelName: String, hostelAddress: String) {
        val pdfDocument = PdfDocument()
        // Page info: A4 size is roughly 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // 1. Draw Title & Hostel Info
        paint.color = Color.parseColor("#0C1B33") // DormiqNavy
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText(hostelName.uppercase(), 40f, 60f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText(hostelAddress, 40f, 80f, paint)
        canvas.drawText("Dormiq Rent Receipt · Generated on behalf of the owner", 40f, 96f, paint)

        // Divider
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(40f, 112f, 555f, 114f, paint)

        // 2. Receipt Info
        paint.color = Color.parseColor("#333333")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Receipt Details", 40f, 145f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Receipt ID: ${payment.id.take(8).uppercase()}", 40f, 170f, paint)
        canvas.drawText("Month: ${payment.month}", 40f, 190f, paint)
        canvas.drawText("Payment Date: ${payment.paidDate ?: "N/A"}", 40f, 210f, paint)
        canvas.drawText("Transaction ID: ${payment.transactionId ?: "N/A"}", 40f, 230f, paint)

        // Tenant / Room Info
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#333333")
        canvas.drawText("Tenant Information", 300f, 145f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Name: ${payment.tenantName}", 300f, 170f, paint)
        canvas.drawText("Room Number: ${payment.roomNumber}", 300f, 190f, paint)
        canvas.drawText("Floor: ${payment.floorName}", 300f, 210f, paint)

        // Divider
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(40f, 260f, 555f, 262f, paint)

        // 3. Billing Summary (Table layout)
        paint.color = Color.parseColor("#F5F7FA")
        canvas.drawRect(40f, 280f, 555f, 320f, paint)

        paint.color = Color.parseColor("#0C1B33")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Description", 50f, 305f, paint)
        canvas.drawText("Status", 350f, 305f, paint)
        canvas.drawText("Amount", 480f, 305f, paint)

        // Row contents
        paint.color = Color.BLACK
        paint.isFakeBoldText = false
        canvas.drawText("Hostel Accommodation Rent - ${payment.month}", 50f, 350f, paint)
        canvas.drawText("PAID", 350f, 350f, paint)
        canvas.drawText("₹${payment.paidAmount}", 480f, 350f, paint)

        // Total
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(40f, 380f, 555f, 382f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#2E7D32") // Safe green
        canvas.drawText("Total Paid: ₹${payment.paidAmount}", 380f, 420f, paint)

        // 4. Footer & Signature
        paint.textSize = 10f
        paint.color = Color.GRAY
        paint.isFakeBoldText = false
        canvas.drawText("Thank you for your prompt payment!", 40f, 490f, paint)
        canvas.drawText("Dormiq App - Sleek, digital and antigravity hostel living.", 40f, 510f, paint)

        pdfDocument.finishPage(page)

        // Write PDF file
        val cacheDir = context.cacheDir
        val receiptFile = File(cacheDir, "Dormiq_Receipt_${payment.month.replace(" ", "_")}.pdf")

        try {
            val fos = FileOutputStream(receiptFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            // Open or share the PDF file
            val fileUri = FileProvider.getUriForFile(
                context,
                "com.dormiq.fileprovider",
                receiptFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Download/Share Receipt"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate receipt: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
