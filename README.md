# BlitzBasicDemos
Amiga Blitz Basic demos

Random bits and pieces, including:

# Transluency demo
NEW! An adaptation of the AMOS demo by Mike at Bit Beam Cannon. Demonstrates a transluent glass effect by blitting to four or five of six bitplanes, as well as the clever EHB palette layout involved.

![Translucency](i.imgur.com/mPcONzy.gif)

The original AMOS demo, as well as a video demonstrating the effect, can be found at https://bitbeamcannon.com/amiga/ under "Amiga OCS/ECS Graphical Specs"

Note that the instructions on the IFF haven't been updated and still refer to AMOS commands.


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

# Interlaced demo
An example of how to display an AGA (8 bitplane) 640x512 image using both high res and interlace.

# CD32 Table of contents
An example of how to read the table of contents from a CD. Note that the calculated track lengths are not reliable, they seem on average 10 seconds longer than the real track lengths are, perhaps due to gaps put between tracks on the disc.
