// Fugue audio engine

goog.provide('engine')

engine.audioEngine = function() {
  var audio = {}
  audio.actx = new AudioContext()
  audio.nodes = []

  audio.out = function(input) {
    input.connect(this.actx.destination)
  }

  audio.gain = function(input, gain) {
    var gainNode = this.actx.createGain()
    gainNode.gain.value = gain
    input.connect(gainNode)
    return gainNode
  }

  audio.sinosc = function(freq) {
    var oscNode = this.actx.createOscillator()
    oscNode.type = 'sine'
    oscNode.frequency.value = freq
    // this.nodes.push(oscNode)
    oscNode.start()
    console.log('playing')
    return oscNode
  }
  return audio;
}
