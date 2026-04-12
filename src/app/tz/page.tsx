import { CountryHomePage } from '@/components/country-home'

export const metadata = {
  title: 'OnDevice AI Tanzania — AI Bila Mtandao | TSh 2,700 Mara Moja',
  description:
    'AI yenye nguvu inayofanya kazi moja kwa moja kwenye simu yako ya Android. Lipa TSh 2,700 mara moja kwa M-Pesa. Hakuna ada ya kila mwezi.',
  alternates: {
    canonical: 'https://on-device.org/tz',
  },
}

export default function TanzaniaPage() {
  return (
    <CountryHomePage
      config={{
        currency: 'TZS',
        countryName: 'Tanzania',
        countryCode: 'tz',
        paymentMethod: 'M-Pesa',
        studentBody:
          '"Study for your CSEE exams anywhere — on the daladala, between classes, even when the internet is slow or unavailable."',
      }}
    />
  )
}
