#!/bin/bash
# Create simple placeholder icons using ImageMagick convert if available
# Otherwise create empty files as placeholders

create_icon() {
    local size=$1
    local output=$2
    
    # Try to create with ImageMagick
    if command -v convert &> /dev/null; then
        convert -size ${size}x${size} xc:#3F51B5 "$output"
    else
        # Create empty file as placeholder
        touch "$output"
    fi
}

# Create icons for different densities
create_icon 48 "app/src/main/res/mipmap-mdpi/ic_launcher.png"
create_icon 48 "app/src/main/res/mipmap-mdpi/ic_launcher_round.png"
create_icon 72 "app/src/main/res/mipmap-hdpi/ic_launcher.png"
create_icon 72 "app/src/main/res/mipmap-hdpi/ic_launcher_round.png"
create_icon 96 "app/src/main/res/mipmap-xhdpi/ic_launcher.png"
create_icon 96 "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png"
create_icon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher.png"
create_icon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png"
create_icon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
create_icon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"

echo "Icons created"
