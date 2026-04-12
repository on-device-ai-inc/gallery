import { CountryHomePage } from '@/components/country-home'

export const metadata = {
  title: 'OnDevice AI Nigeria — AI Without Internet | ₦1,600 One-Time',
  description:
    'Powerful AI that runs on your Android phone. Pay ₦1,600 once — no subscription, no data needed. Works on Tecno, Infinix, and Samsung A-series.',
  alternates: {
    canonical: 'https://on-device.org/ng',
  },
}

export default function NigeriaPage() {
  return (
    <CountryHomePage
      config={{
        currency: 'NGN',
        countryName: 'Nigeria',
        countryCode: 'ng',
        paymentMethod: 'Card',
        missionText: 'No wahala. AI that runs on your phone, on your terms.',
        studentBody:
          '"Prepare for your WAEC and JAMB exams anywhere — on the bus, between lectures, even without airtime or data."',
      }}
    />
  )
}
