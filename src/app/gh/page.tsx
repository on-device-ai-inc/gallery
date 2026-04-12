import { CountryHomePage } from '@/components/country-home'

export const metadata = {
  title: 'OnDevice AI Ghana — AI Without Internet | GH₵15 One-Time',
  description:
    'Powerful AI that runs on your Android phone. Pay GH₵15 once with MTN MoMo or card — no subscription, no data needed after setup.',
  alternates: {
    canonical: 'https://on-device.org/gh',
  },
}

export default function GhanaPage() {
  return (
    <CountryHomePage
      config={{
        currency: 'GHS',
        countryName: 'Ghana',
        countryCode: 'gh',
        paymentMethod: 'MTN MoMo',
        studentBody:
          '"Prepare for your WASSCE exams anywhere — on the trotro, between classes, even without mobile data."',
      }}
    />
  )
}
