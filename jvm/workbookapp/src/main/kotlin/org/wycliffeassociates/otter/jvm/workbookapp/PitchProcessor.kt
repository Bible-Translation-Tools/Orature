package be.tarsos.dsp.example

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.PitchShifter
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.io.jvm.AudioPlayer
import be.tarsos.dsp.resample.Resampler
import java.awt.BorderLayout
import java.io.File
import java.lang.reflect.InvocationTargetException
import javax.sound.sampled.LineUnavailableException
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeListener

class TimeStrechingBasedOnPitchShifting : JFrame() {
    private val fileChooser: JFileChooser
    private lateinit var factorSlider: JSlider
    private lateinit var factorLabel: JLabel
    private var currentFactor = 1.2 // pitch shift factor
    private var dispatcher: AudioDispatcher? = null
    private var pitchShifter: PitchShifter? = null
    private lateinit var buffer: FloatArray
    private val parameterSettingChangedListener = ChangeListener {
        currentFactor = factorSlider.value / 100.0
        factorLabel.text = "Factor " + Math.round(currentFactor * 100) + "%"
        if (this@TimeStrechingBasedOnPitchShifting.dispatcher != null) {
            pitchShifter!!.setPitchShiftFactor(currentFactor.toFloat())
        }
    }

    private fun startFile(file: File) {
        val size = 2048
        val overlap = 2048 - 128
        val samplerate = 44100
        val d = AudioDispatcherFactory.fromPipe(file.absolutePath, samplerate, size, overlap)
        pitchShifter = PitchShifter(1.0 / currentFactor, samplerate.toDouble(), size, overlap)
        d.addAudioProcessor(object : AudioProcessor {
            override fun processingFinished() {
                // TODO Auto-generated method stub
            }

            override fun process(audioEvent: AudioEvent): Boolean {
                buffer = audioEvent.floatBuffer
                return true
            }
        })
        d.addAudioProcessor(pitchShifter)
        d.addAudioProcessor(object : AudioProcessor {
            var r = Resampler(false, 0.1, 4.0)
            override fun processingFinished() {}
            override fun process(audioEvent: AudioEvent): Boolean {
                val factor = currentFactor.toFloat()
                val src = audioEvent.floatBuffer
                val out = FloatArray(((size - overlap) * factor).toInt())
                r.process(factor.toDouble(), src, overlap, size - overlap, false, out, 0, out.size)
                //The size of the output buffer changes (according to factor).
                d.setStepSizeAndOverlap(out.size, 0)
                audioEvent.floatBuffer = out
                audioEvent.overlap = 0
                return true
            }
        })
        //d.addAudioProcessor(rateTransposer);
        try {
            d.addAudioProcessor(AudioPlayer(d.format))
        } catch (e: LineUnavailableException) {
            e.printStackTrace()
        }
        d.addAudioProcessor(object : AudioProcessor {
            override fun processingFinished() {}
            override fun process(audioEvent: AudioEvent): Boolean {
                d.setStepSizeAndOverlap(size, overlap)
                d.setAudioFloatBuffer(buffer)
                audioEvent.floatBuffer = buffer
                audioEvent.overlap = overlap
                return true
            }
        })
        dispatcher = d
        Thread(d).start()
    }

    fun name(): String {
        return "TimeStrechingBasedOnPitchShifting"
    }

    fun description(): String {
        return "Shows how to do time stretching by pitch shifting and resampling"
    }

    fun start() {
        try {
            SwingUtilities.invokeAndWait {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                } catch (e: Exception) {
                    //ignore failure to set default look en feel;
                }
                val frame: JFrame = TimeStrechingBasedOnPitchShifting()
                frame.pack()
                frame.setSize(400, 450)
                frame.isVisible = true
            }
        } catch (e: InvocationTargetException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: InterruptedException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = -7188163235158960778L
        @JvmStatic
        fun main(args: Array<String>) {
            TimeStrechingBasedOnPitchShifting().start()
        }
    }

    init {
        this.layout = BorderLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        title = "Pitch shifting: change the tempo of your audio."
        currentFactor = 1.0
        val fileChooserPanel = JPanel(BorderLayout())
        fileChooserPanel.border = TitledBorder("1... Or choose your audio (wav mono)")
        fileChooser = JFileChooser()
        val chooseFileButton = JButton("Choose a file...")
        chooseFileButton.addActionListener {
            val returnVal = fileChooser.showOpenDialog(this@TimeStrechingBasedOnPitchShifting)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                startFile(file)
            } else {
                //canceled
            }
        }
        fileChooserPanel.add(chooseFileButton)
        fileChooser.layout = BoxLayout(fileChooser, BoxLayout.PAGE_AXIS)
        this.add(fileChooserPanel, BorderLayout.NORTH)
        val params = JPanel(BorderLayout())
        params.border = TitledBorder("2. Set the algorithm parameters")
        factorSlider = JSlider(20, 250)
        factorSlider.value = (currentFactor * 100).toInt()
        factorSlider.paintLabels = true
        factorSlider.addChangeListener(parameterSettingChangedListener)
        val label = JLabel("Factor 100%")
        label.text = "Factor " + Math.round(currentFactor * 100) + "%"
        label.toolTipText = "The tempo factor in % (100 is no change, 50 is double tempo, 200 half)."
        factorLabel = label
        params.add(label, BorderLayout.NORTH)
        params.add(factorSlider, BorderLayout.CENTER)
        this.add(params, BorderLayout.CENTER)
    }
}
