
import { ArrowLeft, CheckCircle2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import {
  getDecisionOptions,
  getPollDetails,
  getVoteStatus,
  submitVote as submitVoteToBackend,
} from '../services/decisionService'

export default function Vote() {
  const { decisionId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { decisions, votes } = useDecisions()

  const decision = decisions.find(
    (item) => String(item.id) === String(decisionId)
  )

  const [selected, setSelected] = useState('')
  const [selectedMultiple, setSelectedMultiple] = useState([])
  const [rating, setRating] = useState(null)
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')
  const [backendOptions, setBackendOptions] = useState([])
  const [pollDetails, setPollDetails] = useState(null)
  const [loading, setLoading] = useState(true)
  const [hasVoted, setHasVoted] = useState(false)

  const pollType = (
    pollDetails?.pollType || decision?.pollType || ''
  ).toUpperCase()

  useEffect(() => {
    const loadPollData = async () => {
      try {
        setLoading(true)

        const [options, poll, voteStatus] = await Promise.all([
          getDecisionOptions(decisionId),
          getPollDetails(decisionId),
          getVoteStatus(decisionId),
        ])

        setBackendOptions(options || [])
        setPollDetails(poll)
        setHasVoted(voteStatus.hasVoted)
      } catch (error) {
        console.error('Failed to load poll data:', error)
        setError('Unable to load voting options. Please try again.')
      } finally {
        setLoading(false)
      }
    }

    if (decisionId) {
      loadPollData()
    }
  }, [decisionId])

  if (!decision) {
    return (
      <div className="page-block">
        <div className="panel-card empty-state">
          <h2>Decision not found</h2>
          <Link to="/explore" className="button button--primary">
            Explore decisions
          </Link>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="page-block">
        <div className="panel-card empty-state">
          <p>Loading voting options...</p>
        </div>
      </div>
    )
  }

  const existingVote = votes[decision.id]

  const toggleMultipleOption = (optionId) => {
    setSelectedMultiple((previous) =>
      previous.includes(String(optionId))
        ? previous.filter((id) => id !== String(optionId))
        : [...previous, String(optionId)]
    )
    setConfirming(false)
    setError('')
  }

  const submitVote = async () => {
    if (!confirming) {
      if (pollType === 'MULTIPLE_CHOICE') {
        if (selectedMultiple.length === 0) {
          setError('Select at least one option before continuing.')
          return
        }
      } else if (!selected) {
        setError('Select an option before continuing.')
        return
      }

      if (pollType === 'RATING' && rating === null) {
        setError('Please select a rating from 1 to 5.')
        return
      }

      setError('')
      setConfirming(true)
      return
    }

    let selections = []

    if (pollType === 'MULTIPLE_CHOICE') {
      selections = selectedMultiple.map((id) => ({
        optionId: Number(id),
        rating: null,
      }))
    } else if (pollType === 'RATING') {
      selections = [
        {
          optionId: Number(selected),
          rating: rating,
        },
      ]
    } else {
      selections = [
        {
          optionId: Number(selected),
          rating: null,
        },
      ]
    }

    try {
      await submitVoteToBackend(decision.id, selections)

      navigate(`/decision/${decision.id}`, {
        state: {
          ...location.state?.fromState,
          voteConfirmed: true,
        },
      })
    } catch (error) {
      console.error('Vote submission failed:', error)
      setError(
        error.response?.data?.message ||
        'Unable to submit your vote. Please try again.'
      )
    }
  }

  const selectedOptionTitle = backendOptions.find(
    (option) => String(option.id) === selected
  )?.title

  const selectedMultipleTitles = backendOptions
    .filter((option) => selectedMultiple.includes(String(option.id)))
    .map((option) => option.title)

  return (
    <div className="page-block">
      <Link
        to={`/decision/${decision.id}`}
        state={location.state?.fromState || {}}
        className="back-link"
      >
        <ArrowLeft size={15} /> Back to decision
      </Link>

      <PageHeading title="Cast your vote" description={decision.title} />

      {hasVoted || existingVote ? (
        <section className="panel-card vote-confirmed">
          <CheckCircle2 size={26} />
          <h2>Vote already recorded</h2>
          <p>
            Your vote has already been recorded. Each participant can vote once.
          </p>
          <Link
            className="button button--outline"
            to={`/decision/${decision.id}`}
          >
            Return to decision
          </Link>
        </section>
      ) : decision.status !== 'Active' ? (
        <section className="panel-card empty-state">
          <h2>Voting is closed</h2>
          <p>This decision is not accepting votes right now.</p>
        </section>
      ) : (
        <section className="panel-card vote-panel">
          <h2>
            {pollType === 'MULTIPLE_CHOICE'
              ? 'Choose your options'
              : pollType === 'RATING'
                ? 'Choose an option and rate it'
                : 'Choose one option'}
          </h2>

          <p className="muted-copy">
            {pollType === 'MULTIPLE_CHOICE'
              ? 'You can select multiple options. Your vote can only be submitted once.'
              : pollType === 'RATING'
                ? 'Select one option and give it a rating from 1 to 5.'
                : 'Your choice can only be submitted once.'}
          </p>

          <div className="vote-options">
            {backendOptions.map((option) => {
              const optionId = String(option.id)
              const isSelected =
                pollType === 'MULTIPLE_CHOICE'
                  ? selectedMultiple.includes(optionId)
                  : selected === optionId

              return (
                <label
                  className={`vote-option ${isSelected ? 'selected' : ''}`}
                  key={option.id}
                >
                  <input
                    type={
                      pollType === 'MULTIPLE_CHOICE'
                        ? 'checkbox'
                        : 'radio'
                    }
                    name="vote-option"
                    value={optionId}
                    checked={isSelected}
                    onChange={() => {
                      if (pollType === 'MULTIPLE_CHOICE') {
                        toggleMultipleOption(optionId)
                      } else {
                        setSelected(optionId)
                        setRating(null)
                        setConfirming(false)
                        setError('')
                      }
                    }}
                  />

                  <span>
                    <strong>{option.title}</strong>
                    {option.description ? (
                      <small>{option.description}</small>
                    ) : null}
                  </span>
                </label>
              )
            })}
          </div>

          {pollType === 'RATING' && selected ? (
            <div className="rating-selector">
              <h3>Rate your selected option</h3>
              <p className="muted-copy">Select a rating from 1 to 5</p>

              <div className="form-footer-actions">
                {[1, 2, 3, 4, 5].map((value) => (
                  <button
                    key={value}
                    type="button"
                    className={`button ${
                      rating === value
                        ? 'button--primary'
                        : 'button--outline'
                    }`}
                    onClick={() => {
                      setRating(value)
                      setConfirming(false)
                      setError('')
                    }}
                  >
                    {value}
                  </button>
                ))}
              </div>
            </div>
          ) : null}

          {confirming ? (
            <div className="vote-confirmation" role="status">
              <strong>
                {pollType === 'MULTIPLE_CHOICE'
                  ? `Confirm your votes for: ${selectedMultipleTitles.join(', ')}?`
                  : pollType === 'RATING'
                    ? `Confirm ${selectedOptionTitle} with rating ${rating}/5?`
                    : `Confirm your vote for "${selectedOptionTitle}"?`}
              </strong>
              <span>You won’t be able to change it after submitting.</span>
            </div>
          ) : null}

          {error ? (
            <p className="form-alert" role="alert">
              {error}
            </p>
          ) : null}

          <div className="form-footer-actions">
            {confirming ? (
              <button
                type="button"
                className="button button--outline"
                onClick={() => setConfirming(false)}
              >
                Change selection
              </button>
            ) : null}

            <button
              type="button"
              className="button button--primary"
              onClick={submitVote}
            >
              {confirming ? 'Confirm Vote' : 'Continue'}
            </button>
          </div>
        </section>
      )}
    </div>
  )
}
