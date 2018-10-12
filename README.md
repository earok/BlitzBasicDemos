# BlitzBasicDemos
Amiga Blitz Basic demos

Random bits and pieces, including:

# 1942 tutorial
The downloads for the 1942 tutorials that were hosted by Amiten.TV

# Splatoon 8 bit Ebb and Flow AGA
An attempt to adapt the Splatoon fan video by jtangc and Lumena-Tan to AGA Amiga
Original: https://youtu.be/IXASL0aAAU4
Amiga version: https://youtu.be/o4zDDVjbCAo

# Sprite Multiplexing demo
A rudimentary (and not exactly finished) Sprite Multiplexing demo. Compared to the built in Blitz "CustomSprites" option, it:
- Offers virtually limitless sprites, rather than just an additional 8 (still limited to no more than 8 per scanline)
- Doesn't need a point where there's a clean break between top and bottom sprites - sprites can be staggered.

It's pretty rough though - it only supports attached (16 colour) sprites and doesn't fully support all Sprite features (high resolution positioning etc). I only really made this for a very bespoke purpose, but it should be enough to help you get started on your own multiplexing solution if you need something a bit more powerful than what Blitz offers built in.

# Interleaving demo
An extremely rudimentary demo of how Interleaved mode can be achieved in Blitz without any external libraries. What I've provided should be enough to get you started with a tidier solution.

Interleaved graphics have an advantage over the standard Amiga graphics setup in that a single blit can be used to put a shape on a bitmap, regardless of the number of blitplanes. But there's also a disadvantage in that much more memory is required for cookies. In short - if you can spare the chip memory, this should give you a speed boost.

Codetapper's site gives a good explanation of different Amiga graphics modes: http://codetapper.com/amiga/maptapper/documentation/gfx/gfx-mode/
