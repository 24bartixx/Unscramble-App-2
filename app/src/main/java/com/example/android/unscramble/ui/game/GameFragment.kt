/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.example.android.unscramble.ui.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.unscramble.R
import com.example.android.unscramble.databinding.GameFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Fragment where the game is played, contains the game logic.
 */
class GameFragment : Fragment() {

    // viewModel GameViewModel initialization
    // property delegation ( by <delegate-class>() ) helps to handoff the getter-setter responsibility to a different class
    // without it the app will lose the state of gameViewModel reference when the device goes through configuration changes

    // by viewModels() creates refers to delegate class which handoff the getter-setter responsibility to class
    // called viewModels, so that the app will not lose state of the viewModel reference when the device goes through
    // configuration change
    private val gameViewModel : GameViewModel by viewModels()


    // Binding object instance with access to the views in the game_fragment.xml layout
    private lateinit var binding: GameFragmentBinding

    // Create a ViewModel the first time the fragment is created.
    // If the fragment is re-created, it receives the same GameViewModel instance created by the
    // first fragment

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        // Inflate the layout XML file and return a binding object instance
        binding = GameFragmentBinding.inflate(inflater, container, false)

        Log.d("GameFragment", "GameFragment created!")
        Log.d("GameFragment", "Word: ${gameViewModel.currentScrambledWord}\tWord " +
                "Count: ${gameViewModel.currentWordCount}\tScore: ${gameViewModel.score}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup a click listener for the Submit and Skip buttons.
        binding.submit.setOnClickListener { onSubmitWord() }
        binding.skip.setOnClickListener { onSkipWord() }
        // Update the UI
        gameViewModel.score.observe(viewLifecycleOwner,
            { newScore -> binding.score.text = getString(R.string.score, newScore)})
        gameViewModel.currentWordCount.observe(viewLifecycleOwner,
            { newCount -> binding.wordCount.text = getString(R.string.word_count, newCount, MAX_WORDS)})
        // setting observer for LiveData object
        // viewLifecycleOwner is a representation of Fragment's View lifecycle
        gameViewModel.currentScrambledWord.observe(viewLifecycleOwner,
            { newWord -> binding.textViewUnscrambledWord.text = newWord })
    }

    /*
    * Checks the user's word, and updates the score accordingly.
    * Displays the next scrambled word.
    */
    private fun onSubmitWord() {
        val playerWord = binding.textInputEditText.text.toString()

        if(gameViewModel.checkWord(playerWord)) {
            setErrorTextField(false)
            if (!gameViewModel.isThereNextWord()) finalScoreDialog()
        }

        else {
            setErrorTextField(true)
        }


    }

    /*
     * Skips the current word without changing the score.
     * Increases the word count.
     */
    private fun onSkipWord() {
        if(gameViewModel.isThereNextWord()){
            setErrorTextField(false)
            Log.d("WTF", "${gameViewModel.currentWordCount}")
        }
        else {
            finalScoreDialog()
        }
    }


    //Displays the next scrambled word on screen.
    private fun updateNextWordOnScreen() {
        binding.textViewUnscrambledWord.text = gameViewModel.currentScrambledWord.value
    }

    /*
     * Gets a random word for the list of words and shuffles the letters in it.
     */
    private fun getNextScrambledWord(): String {
        val tempWord = allWordsList.random().toCharArray()
        tempWord.shuffle()
        return String(tempWord)
    }


    /*
    * Sets and resets the text field error status.
    */
    private fun setErrorTextField(error: Boolean) {
        if (error) {
            binding.textField.isErrorEnabled = true
            binding.textField.error = getString(R.string.try_again)
        } else {
            binding.textField.isErrorEnabled = false
            binding.textInputEditText.text = null
        }
    }

    // display final score dialog
    private fun finalScoreDialog() {
        // MaterialAlertDialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.congratulations))
            .setMessage(getString(R.string.you_scored, gameViewModel.score.value))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.exit)) { _, _ -> exitGame() }
            .setPositiveButton(getString(R.string.play_again)) {_, _ -> restartGame()}
            .show()
    }

    // exit the game
    private fun exitGame() { activity?.finish() }

    // re-initializes the data in the ViewModel and updates the views with the new data, to restart the game.
    private fun restartGame() {
        gameViewModel.reinitializeData()
        setErrorTextField(false)
    }

    // called when the fragment is no longer attached to an activity, called after onDestroy()
    override fun onDetach(){
        super.onDetach()
        Log.d("GameFragment", "GameFragment destroyed!")
    }
}
