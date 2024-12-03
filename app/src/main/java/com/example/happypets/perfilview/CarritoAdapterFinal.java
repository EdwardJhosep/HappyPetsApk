package com.example.happypets.perfilview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;  // Import Glide for image loading
import com.example.happypets.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.graphics.pdf.PdfDocument;

public class CarritoAdapterFinal extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> carritoItems;
    private String token;
    private String userId;

    public CarritoAdapterFinal(Context context, ArrayList<JSONObject> carritoItems, String token, String userId) {
        this.context = context;
        this.carritoItems = carritoItems;
        this.token = token;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return carritoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return carritoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_carrito, parent, false);
        }

        try {
            // Obtener el ítem actual
            JSONObject item = carritoItems.get(position);

            // Configurar la cantidad, color y otros detalles en los TextViews
            TextView cantidadTextView = convertView.findViewById(R.id.textViewCantidad);
            cantidadTextView.setText("Cantidad: " + item.optString("cantidad", "N/A"));

            TextView colorTextView = convertView.findViewById(R.id.textViewColor);
            colorTextView.setText("Color: " + item.optString("color", "No especificado"));

            TextView importeTextView = convertView.findViewById(R.id.textViewImporte);
            importeTextView.setText("Importe: " + item.optString("importe", "N/A"));

            TextView tipoPagoTextView = convertView.findViewById(R.id.textViewTipoPago);
            tipoPagoTextView.setText("Tipo de Pago: " + item.optString("tipo_pago", "No especificado"));

            // Mostrar solo la parte de la fecha de updated_at
            TextView updatedAtTextView = convertView.findViewById(R.id.textViewFecha);
            String updatedAt = item.optString("updated_at", "N/A");

            // Formatear updated_at para mostrar solo la fecha
            if (!updatedAt.equals("N/A")) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(updatedAt);
                updatedAtTextView.setText("Fecha: " + outputFormat.format(date));
            } else {
                updatedAtTextView.setText("Fecha: N/A");
            }

            // Obtener detalles del producto: nombre e imagen
            String productoNombre = item.optString("producto_nombre", "Producto no disponible");
            String productoImagen = item.optString("producto_imagen", "");

            // Configurar el nombre del producto en el TextView
            TextView productoNombreTextView = convertView.findViewById(R.id.textViewProductoNombre);
            productoNombreTextView.setText(productoNombre);

            // Configurar la imagen del producto en el ImageView
            ImageView productoImageView = convertView.findViewById(R.id.imageViewProducto);
            if (!productoImagen.isEmpty()) {
                String imageUrl = "https://api.happypetshco.com/ServidorProductos/" + productoImagen;
                Glide.with(context)
                        .load(imageUrl)
                        .into(productoImageView);
            }

            // Configurar el código de operación
            TextView codigoOperacionTextView = convertView.findViewById(R.id.textViewCodigoOperacion);
            String codigoOperacion = item.optString("codigo_operacion", "No disponible");
            codigoOperacionTextView.setText(codigoOperacion);

            // Botón para generar el PDF de este ítem
            convertView.findViewById(R.id.btnGenerarPdf).setOnClickListener(v -> {
                // Mostrar diálogo de confirmación antes de generar el PDF
                new AlertDialog.Builder(context)
                        .setTitle("Generar PDF")
                        .setMessage("¿Está seguro de que desea generar la boleta de venta?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            try {
                                // Pasar el ítem actual para generar el PDF
                                generatePdf(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private void generatePdf(JSONObject item) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        // Logo pequeño
        Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.boleta);
        int desiredWidth = 100;
        int desiredHeight = (int) (logoBitmap.getHeight() * ((float) desiredWidth / logoBitmap.getWidth()));
        Bitmap scaledLogoBitmap = Bitmap.createScaledBitmap(logoBitmap, desiredWidth, desiredHeight, true);
        canvas.drawBitmap(scaledLogoBitmap, 40, 40, paint);

        int lineHeight = 100;

        paint.setColor(Color.BLUE);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Boleta de Venta - HappyPets ", 150, lineHeight, paint);
        lineHeight += 30;

        // Información adicional de la empresa
        paint.setTextSize(12);
        paint.setColor(Color.DKGRAY);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("HappyPets - Huánuco", 40, lineHeight, paint);
        lineHeight += 20;
        canvas.drawText("RUC: 10442463820", 40, lineHeight, paint);
        lineHeight += 20;
        canvas.drawText("Distrito: Huánuco", 40, lineHeight, paint);
        lineHeight += 20;
        canvas.drawText("Provincia: Huánuco", 40, lineHeight, paint);
        lineHeight += 20;
        canvas.drawText("Departamento: Huánuco", 40, lineHeight, paint);
        lineHeight += 20;
        canvas.drawText("Dirección: Jr. Aguilar Nro. 649 - Huánuco", 40, lineHeight, paint);
        lineHeight += 40;

        // Mostrar el código de operación
        String codigoOperacion = item.optString("codigo_operacion", "No disponible");
        paint.setColor(Color.RED);
        paint.setTextSize(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Código de Operación: " + codigoOperacion, 40, lineHeight, paint);
        lineHeight += 20;

        // Línea separadora
        paint.setStrokeWidth(2);
        paint.setColor(Color.GRAY);
        canvas.drawLine(40, lineHeight, 555, lineHeight, paint);
        lineHeight += 20;

        // Encabezado de la tabla
        String[] headers = {"Producto", "Cantidad", "Importe"};
        float totalWidth = 515; // Espacio disponible para la tabla (555 - 40)
        float[] columnWidths = new float[headers.length];

        // Calcular los anchos de las columnas proporcionalmente
        float totalHeaderLength = 0;
        for (String header : headers) {
            totalHeaderLength += paint.measureText(header);  // Medir el ancho de cada encabezado
        }

        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = (paint.measureText(headers[i]) / totalHeaderLength) * totalWidth;
        }

        // Dibujar el encabezado con color
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.WHITE);
        float xPos = 40;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawRect(xPos, lineHeight - 15, xPos + columnWidths[i], lineHeight + 10, paint);
            xPos += columnWidths[i];
        }

        paint.setColor(Color.BLACK);
        xPos = 40;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], xPos + 5, lineHeight, paint);
            xPos += columnWidths[i];
        }

        lineHeight += 30;

        // Línea separadora
        paint.setColor(Color.GRAY);
        canvas.drawLine(40, lineHeight, 555, lineHeight, paint);
        lineHeight += 20;

        // Datos de la tabla
        String productoNombre = item.optString("producto_nombre", "Producto no disponible");
        String cantidad = item.optString("cantidad", "N/A");
        String importe = item.optString("importe", "N/A");

        String[] rowData = {productoNombre, cantidad, importe};
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(Color.BLACK);
        xPos = 40;
        for (int i = 0; i < rowData.length; i++) {
            canvas.drawText(rowData[i], xPos, lineHeight, paint);
            xPos += columnWidths[i];  // Ajustar según el ancho de la columna
        }

        lineHeight += 40;

        // Línea final
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        canvas.drawLine(40, lineHeight, 555, lineHeight, paint);
        lineHeight += 20;

        // Pie de página
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(14);
        paint.setColor(Color.GREEN);
        canvas.drawText("¡Gracias por confiar en HappyPets!", 40, lineHeight, paint);
        lineHeight += 20;

        document.finishPage(page);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = sdf.format(new Date());

        String fileName = "boleta_" + date + "_" + productoNombre + ".pdf";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            document.close();
            // Mostrar notificación
            showNotification("PDF generado exitosamente", "Boleta de venta guardada en Descargas");

            // Abrir el PDF automáticamente
            openPdf(file);
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Error al generar el PDF", "Hubo un problema al guardar el PDF.");
        }
    }
    private void openPdf(File file) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }



    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear canal de notificación (requerido para Android 8.0 y superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "happy_pets_channel";
            CharSequence channelName = "HappyPets Notificaciones";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "happy_pets_channel")
                .setSmallIcon(R.drawable.pdf) // Reemplazar con tu icono
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}