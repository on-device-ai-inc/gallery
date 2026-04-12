import { CountryHomePage } from '@/components/country-home'

export const metadata = {
  title: 'OnDevice AI South Africa — AI on Your Phone | R18 One-Time',
  description:
    'Powerful AI that runs entirely on your Android phone. Pay R18 once — no subscription, no data needed after setup. Works on most Android phones.',
  alternates: {
    canonical: 'https://on-device.org/za',
  },
}

export default function SouthAfricaPage() {
  return (
    <CountryHomePage
      config={{
        currency: 'ZAR',
        countryName: 'South Africa',
        countryCode: 'za',
        paymentMethod: 'Card',
        studentBody:
          '"Study for your Matric exams anywhere — on the taxi, between classes, even when your data runs out."',
      }}
    />
  )
}
