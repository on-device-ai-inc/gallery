import { CountryHomePage } from '@/components/country-home'

export const metadata = {
  title: 'OnDevice AI Zimbabwe — AI on Your Phone | From $3.99',
  description:
    'Powerful AI that runs entirely on your Android phone. Pay once — no subscription, no data needed. Prepare for ZIMSEC exams without Wi-Fi.',
  alternates: {
    canonical: 'https://on-device.org/zw',
  },
}

export default function ZimbabwePage() {
  return (
    <CountryHomePage
      config={{
        currency: 'USD',
        countryName: 'Zimbabwe',
        countryCode: 'zw',
        paymentMethod: 'Card',
        studentBody:
          '"Study for your ZIMSEC O-Level and A-Level exams anywhere — between classes, even when the network is down."',
      }}
    />
  )
}
