#!/usr/bin/env bash
set -euo pipefail

# Build a dual-audio KTV video from a source MV. The second track attenuates
# center-panned vocals by subtracting the opposing stereo channel.
input="${1:?Usage: $0 <source-video> <output-video>}"
output="${2:?Usage: $0 <source-video> <output-video>}"

ffmpeg -hide_banner -loglevel error -y \
  -i "$input" \
  -filter_complex '[0:a]asplit=2[original][stereo];[stereo]pan=stereo|c0=c0-c1|c1=c1-c0,volume=1.414214[accompaniment]' \
  -map 0:v:0 -map '[original]' -map '[accompaniment]' \
  -c:v copy -c:a aac -b:a 192k \
  -metadata:s:a:0 title='原唱' -metadata:s:a:0 language=zho \
  -metadata:s:a:1 title='伴奏' -metadata:s:a:1 language=zho -disposition:a:1 karaoke \
  -movflags +faststart \
  "$output"
