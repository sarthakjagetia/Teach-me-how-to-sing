# -*- coding: utf-8 -*-
"""
Created on Fri Nov 11 10:33:50 2016
Give the program a song, 
then run pitch detection. 
Outputs a file with all the pitches, their start times, and durations.
format:
[Seconds into song where note that is sung begins] [Pitch of note (frequency, Hz)] [Duration of note, milliseconds] 
0.5s 550Hz 500ms
1.0s 260Hz 1000ms
2.0s 440Hz 250ms 
@author: sarjag
"""
import pyaudio
import wave
import sys

# length of data to read.
chunk = 1024

# validation. If a wave file hasn't been specified, exit.
if len(sys.argv) < 2:
    print ("Plays a wave file.\n\n" +\
          "Usage: %s filename.wav" % sys.argv[0])
    sys.exit(-1)

'''
************************************************************************
      This is the start of the "minimum needed to read a wave"
************************************************************************
'''
# open the file for reading.
wf = wave.open(sys.argv[1], 'rb')

# create an audio object
p = pyaudio.PyAudio()

# open stream based on the wave object which has been input.
stream = p.open(format =
                p.get_format_from_width(wf.getsampwidth()),
                channels = wf.getnchannels(),
                rate = wf.getframerate(),
                output = True)

# read data (based on the chunk size)
data = wf.readframes(chunk)
print (data)
# play stream (looping from beginning of file to the end)
while data != '':
    # writing to the stream is what *actually* plays the sound.
    stream.write(data)
    data = wf.readframes(chunk)

# cleanup stuff.
stream.close()    
p.terminate()