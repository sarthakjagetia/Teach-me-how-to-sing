import matplotlib.pyplot as plt
#import numpy as np
import wave
#import sys
from scipy.io.wavfile import read
import numpy
maxi=0
rate,song = read('We_mono.wav')
a=wave.open('We_mono.wav')
#song= numpy.array(song, dtype=float)
song1=song[0:10000]

def loudest_band(music,frame_rate,bandwidth):
	max_energy = 0
	dft = numpy.fft.fft(music)
	scaled_bandwidth = int(bandwidth * music.size / frame_rate)
	#print(scaled_bandwidth)
	for i in range(int(dft.size / 2 - scaled_bandwidth)):
		en = numpy.sum(numpy.square(numpy.absolute(dft[i:i + scaled_bandwidth])))
		if(en > max_energy):
			max_energy = en
			dp = i
	loudest = numpy.zeros(dft.size,dtype=complex)
	loudest[dp:dp  + scaled_bandwidth] = dft[dp:dp + scaled_bandwidth]
	for i in range(scaled_bandwidth):
		loudest[-(dp + i)] = numpy.conj(dft[dp + i])
	loudest_time_sg = numpy.fft.ifft(loudest);print(loudest_time_sg);print ((frame_rate * dp / music.size));print(frame_rate * dp / music.size + bandwidth)
     

loudest_band(song,44100,30)

#for i in range (int(song1.size/2)):
#    dft=numpy.fft.fft(song1)
#    en=numpy.square(numpy.absolute(dft))
#    plt.plot(en)
#    if(en[i]>maxi):
#        maxi = en[i];

#print("The pitch for %d samples is %d" %(song1.size,maxi))
    
#dft=numpy.fft.fft(song)


##Extract Raw Audio from Wav File
#signal = spf.readframes(-1)
#signal = np.fromstring(signal, 'Int16')
#
#plt.figure(1)
#plt.title('Signal Wave...')
#plt.plot(signal)
#plt.show()