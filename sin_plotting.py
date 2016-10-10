import numpy as np
import matplotlib.pyplot as plt


#sampling frequency, 1/seconds
Fs = 1000
#remember that the the nyquist frequency is Fs/2

#number of samples
N  = 512

#give a frequency (Hz) for the signal for which we'll compute the FFT
f  = 490

#calculate that signal's period (s)
T = 1/f

#impose a phase offset, radians
phi = np.pi/3

#create time array
#total length of signal in time is N*sampling period.
t = np.arange(0,N/Fs,1/Fs)

#create frequency axis for FFT
#fft bins go from 0 to Fs/N, in intervals of Fs/N
freq = np.arange(0,Fs,Fs/N)

#Create a simple sine signal on which to compute the FFT
signal = np.sin(2*np.pi*t/T+phi)


#Plot the signal vs. time
fig1 = plt.figure(1)
plt.plot(t, signal)
plt.xlabel('Time, s')
plt.ylabel('Amplitude')
plt.suptitle('Signal in time domain')
fig1.show()

#compute the FFT, returns an array of complex numbers
signal_fft = np.fft.fft(signal)

#get the magnitude
signal_fft_abs = np.absolute(signal_fft)

#plot the magnitude vs. frequency
fig2 = plt.figure(2)
plt.plot(freq[0:int(N/2)], signal_fft_abs[0:int(N/2)])
plt.xlabel('Frequency, Hz')
plt.ylabel('Magnitude')
plt.suptitle('Signal in frequency domain')
fig2.show()

#pause to keep figures alive
print("Press enter to close figures")
input()





