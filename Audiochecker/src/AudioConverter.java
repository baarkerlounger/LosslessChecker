import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import org.kc7bfi.jflac.util.WavWriter;

/**
 * Decode FLAC file to WAV file application.
 * @author kc7bfi
 */
public class AudioConverter implements PCMProcessor {
    private WavWriter wav;
    
    /**
     * Decode a FLAC file to a WAV file.
     * @param inFileName    The input FLAC file name
     * @param outFileName   The output WAV file name
     * @throws IOException  Thrown if error reading or writing files
     */
    public void decode(String flacFile, String tempWav) throws IOException {

        FileInputStream inputStream = new FileInputStream(flacFile);
        FileOutputStream outputStream = new FileOutputStream(tempWav);
        wav = new WavWriter(outputStream);
        FLACDecoder decoder = new FLACDecoder(inputStream);
        decoder.addPCMProcessor(this);
        decoder.decode();
    }
    
    /**
     * Process the StreamInfo block.
     * @param info the StreamInfo block
     * @see org.kc7bfi.jflac.PCMProcessor#processStreamInfo(org.kc7bfi.jflac.metadata.StreamInfo)
     */
    public void processStreamInfo(StreamInfo info) {
        try {
            wav.writeHeader(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Process the decoded PCM bytes.
     * @param pcm The decoded PCM data
     * @see org.kc7bfi.jflac.PCMProcessor#processPCM(org.kc7bfi.jflac.util.ByteSpace)
     */
    public void processPCM(ByteData pcm) {
        try {
            wav.writePCM(pcm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
