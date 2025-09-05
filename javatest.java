import com.amivoice.wrp.Wrp;
import com.amivoice.wrp.WrpListener;
import javax.sound.sampled.*;

/**
 * 使用麦克风实时采集音频并传送至 AmiVoice 的示例喵。
 */
public class AmiVoiceMicExample {
    private final Wrp wrp;

    public AmiVoiceMicExample(String serverURL, String authorization, String grammar) {
        wrp = Wrp.construct();
        wrp.setServerURL(serverURL);
        wrp.setAuthorization(authorization);
        // 使用 LSB16K 表示 16kHz、16bit little-endian PCM，无文件头:contentReference[oaicite:1]{index=1}喵
        wrp.setCodec("LSB16K");
        wrp.setGrammarFileNames(grammar);
        // 可选：设置每秒更新一次结果喵
        wrp.setOptionalProperty("resultUpdatedInterval", "1000");
    }

    public void run() {
        wrp.setListener(new WrpListener() {
            @Override public void utteranceStarted(int time) { System.out.println("开始发话: " + time); }
            @Override public void utteranceEnded(int time) { System.out.println("结束发话: " + time); }
            @Override public void resultCreated() {}
            @Override public void resultUpdated(String result) { System.out.println("部分结果: " + result); }
            @Override public void resultFinalized(String result) { System.out.println("最终结果: " + result); }
        });

        // 配置 16kHz、16bit、单声道、little‑endian 音频格式喵
        AudioFormat format = new AudioFormat(16000F, 16, 1, true, false);
        try (TargetDataLine line = AudioSystem.getTargetDataLine(format)) {
            line.open(format);
            line.start();

            // 启动会话喵
            if (!wrp.feedDataResume()) {
                System.err.println("无法开始会话: " + wrp.getLastMessage());
                return;
            }

            byte[] buffer = new byte[4096];
            boolean recording = true;
            while (recording) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    wrp.feedData(buffer, 0, bytesRead);
                    // 可根据需要添加休眠以控制发送速率喵
                }

                // 根据您的终止条件决定何时停止喵，例如监听键盘输入或达到时长
            }

            wrp.feedDataPause(); // 停止发送喵
        } catch (LineUnavailableException e) {
            System.err.println("无法访问麦克风: " + e.getMessage());
        }
        wrp.disconnect();
    }

    @Override
    public String toString() {
        return "AmiVoiceMicExample{}";
    }

    public static void main(String[] args) {
        AmiVoiceMicExample example = new AmiVoiceMicExample(
                "wss://acp-api.amivoice.com/v1/",
                "YOUR_APPKEY",
                "-a-general");
        example.run();
    }
}
