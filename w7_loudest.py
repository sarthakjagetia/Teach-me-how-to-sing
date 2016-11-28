# AUTHOR Ravi Teja  Darbha rdarbha@bu.edu
# AUTHOR Prateek Mehta pmehta59@bu.edu
# AUTHOR Sarthak Jagetia sarthakj@bu.edu
# AUTHOR Aastha Anand aastha24@bu.edu

import numpy
import scipy.io.wavfile as wavfile
import numpy.fft as fft

def loudest_band(music,frame_rate,bandwidth):
	max_energy = 0
	dft = fft.fft(music)
	scaled_bandwidth = int(bandwidth * music.size / frame_rate)
	print(scaled_bandwidth)
	for i in range(int(dft.size / 2 - scaled_bandwidth)):
		en = numpy.sum(numpy.square(numpy.absolute(dft[i:i + scaled_bandwidth])))
		if(en > max_energy):
			max_energy = en
			dp = i
	loudest = numpy.zeros(dft.size,dtype=complex)
	loudest[dp:dp  + scaled_bandwidth] = dft[dp:dp + scaled_bandwidth]
	for i in range(scaled_bandwidth):
		loudest[-(dp + i)] = numpy.conj(dft[dp + i])
	loudest_time_sg = fft.ifft(loudest)
	return (frame_rate * dp / music.size), (frame_rate * dp / music.size + bandwidth),loudest_time_sg

