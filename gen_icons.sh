#!/bin/bash
# SHΔDØW CORE - GENERADOR DE ÍCONOS ZEROCHAT

APP_DIR="app/src/main/res"
TMP_DIR="$HOME/.zerochat_icons_tmp"
mkdir -p $TMP_DIR
rm -rf $APP_DIR/mipmap-*

# 1. RENDERIZADO DEL ÍCONO BASE (512x512)
# Diseño: Fondo oscuro, anillo exterior azul, núcleo interno azul brillante
convert -size 512x512 xc:"#0F0F0F" \
  \( -size 400x400 xc:none -fill "#4D6BFE" -draw "circle 200,200 200,20" \) -gravity center -composite \
  \( -size 320x320 xc:none -fill "#0F0F0F" -draw "circle 160,160 160,20" \) -gravity center -composite \
  \( -size 200x200 xc:none -fill "#4D6BFE" -draw "circle 100,100 100,20" \) -gravity center -composite \
  $TMP_DIR/ic_launcher_foreground_512.png

# Crear fondo plano para adaptive icons (108dp base = 432px a XXXHDPI)
convert -size 432x432 xc:"#0F0F0F" $TMP_DIR/ic_launcher_background.png

# 2. ESCALADO PARA MIPMAPS LEGACY (PNGs clásicos)
declare -A SIZES=(
  ["mdpi"]=48
  ["hdpi"]=72
  ["xhdpi"]=96
  ["xxhdpi"]=144
  ["xxxhdpi"]=192
)

for DENSITY in "${!SIZES[@]}"; do
  SIZE=${SIZES[$DENSITY]}
  DIR="$APP_DIR/mipmap-$DENSITY"
  mkdir -p $DIR
  
  # Ícono cuadrado y redondo (mismo diseño para simplificar)
  convert $TMP_DIR/ic_launcher_foreground_512.png -resize ${SIZE}x${SIZE} $DIR/ic_launcher.png
  cp $DIR/ic_launcher.png $DIR/ic_launcher_round.png
done

# 3. ESCALADO PARA ADAPTIVE ICONS (Foreground y Background)
# Android requiere que el asset adaptive sea de 108dp. 
# En XXXHDPI (4x), 108dp = 432px. Calculamos para cada densidad.
declare -A ADAPTIVE_SIZES=(
  ["mdpi"]=108
  ["hdpi"]=162
  ["xhdpi"]=216
  ["xxhdpi"]=324
  ["xxxhdpi"]=432
)

for DENSITY in "${!ADAPTIVE_SIZES[@]}"; do
  SIZE=${ADAPTIVE_SIZES[$DENSITY]}
  DIR="$APP_DIR/mipmap-$DENSITY"
  
  # Escalar foreground (el diseño de 512 se ajusta al canvas de 108dp)
  convert $TMP_DIR/ic_launcher_foreground_512.png -resize ${SIZE}x${SIZE} $DIR/ic_launcher_foreground.png
  convert $TMP_DIR/ic_launcher_background.png -resize ${SIZE}x${SIZE} $DIR/ic_launcher_background.png
done

# 4. CREAR XML DE ADAPTIVE ICON (API 26+)
mkdir -p $APP_DIR/mipmap-anydpi-v26
cat <<EOF > $APP_DIR/mipmap-anydpi-v26/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
EOF

cat <<EOF > $APP_DIR/mipmap-anydpi-v26/ic_launcher_round.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
EOF

rm -rf $TMP_DIR
echo "[SHADOW] Íconos generados con éxito."
