// Fugue audio engine

goog.provide('engine')

engine.node = null

engine.audioEngine = function() {
  var audio = {}
  audio.actx = new AudioContext()
  audio.node = null
  audio.sinosc = function(freq) {
    var osc = this.actx.createOscillator()
    osc.type = 'sine'
    osc.frequency.value = freq
    this.node = osc
    osc.connect(this.actx.destination)
    osc.start()
    console.log('playing')
  }
  return audio;
}
