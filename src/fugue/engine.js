// Fugue audio engine

goog.provide('fugue.engine')

fugue.engine.ctx = new AudioContext()

/**
 * Creates and plays an OscillatorNode
 * @param {string} type
 * @param {float} freq
 * @return {OscillatorNode} Created node
 */
fugue.engine.osc = function(type, freq) {
  var oscNode = fugue.engine.ctx.createOscillator()
  oscNode.type = type
  oscNode.frequency.value = freq
  oscNode.start()
  console.log('playing')
  return oscNode
}

/**
 * Connects a GainNode to the input
 * @param {AudioNode} input
 * @param {float} gain
 * @return {GainNode} End of chain
 */
fugue.engine.gain = function(input, gain) {
  var gainNode = fugue.engine.ctx.createGain()
  gainNode.gain.value = gain
  input.connect(gainNode)
  return gainNode
}

/**
 * Connects an AudioNode to the audio destination
 * @param {AudioNode} input
 */
fugue.engine.out = function(input) {
  input.connect(fugue.engine.ctx.destination)
}

