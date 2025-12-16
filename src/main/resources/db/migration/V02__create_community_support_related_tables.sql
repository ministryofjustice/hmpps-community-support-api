-- Migration to create the region table and insert predefined regions
CREATE TABLE IF NOT EXISTS region (
    id UUID NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO region (id, name) VALUES
                                  ('32e3685f-15c5-4fdf-ab00-913af2217cb7', 'North East'),
                                  ('3f8a9b0e-6c14-4e2b-9a5f-1d2c3b4a5e6f', 'North West'),
                                  ('e6c1a9b2-3f47-4a90-8d5e-7b2c1a3f4e5d', 'Wales'),
                                  ('9b7a6c5d-2e1f-4b3a-8c9d-0a1b2c3d4e5f', 'South Central'),
                                  ('a1b2c3d4-e5f6-4789-abcd-0123456789ab', 'South West'),
                                  ('b2c3d4e5-f6a7-4b89-9cde-112233445566', 'Kent, Surrey and Sussex'),
                                  ('c3d4e5f6-a7b8-4c90-8d9e-223344556677', 'East Midlands'),
                                  ('d4e5f6a7-b8c9-4d01-9eaf-334455667788', 'West Midlands'),
                                  ('e5f6a7b8-c9d0-4e12-8fb0-445566778899', 'Yorkshire and the Humber'),
                                  ('f6a7b8c9-d0e1-4f23-90c1-556677889900', 'East of England'),
                                  ('0a1b2c3d-4e5f-4678-91ab-66778899aabb', 'London');

-- Migration to create the contract_area table
CREATE TABLE contract_area (
                               id UUID PRIMARY KEY,
                               region_id UUID NOT NULL REFERENCES region(id),
                               area TEXT NOT NULL
);

INSERT INTO contract_area (id, region_id, area) VALUES
-- North East
('f55be492-692d-4b5a-91c0-6f66a372c9bf', '32e3685f-15c5-4fdf-ab00-913af2217cb7', 'Cleveland'),
('82dbe898-3bb8-4cb1-ac5e-1b1a4c4de960', '32e3685f-15c5-4fdf-ab00-913af2217cb7', 'Durham'),
('8b37e790-d077-489a-bfab-7cc7c01d5cc8', '32e3685f-15c5-4fdf-ab00-913af2217cb7', 'Northumbria'),

-- North West
('8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', '3f8a9b0e-6c14-4e2b-9a5f-1d2c3b4a5e6f', 'Cheshire and Merseyside'),
('9b7d6c5a-3f2e-4a1b-8c9d-0f1e2d3c4b5a', '3f8a9b0e-6c14-4e2b-9a5f-1d2c3b4a5e6f', 'Cumbria'),
('a3e4d5c6-b7a8-4c9d-8e1f-2233445566aa', '3f8a9b0e-6c14-4e2b-9a5f-1d2c3b4a5e6f', 'Lancashire'),

-- Wales
('b4c5d6e7-f8a9-4b0c-9d8e-334455667788', 'e6c1a9b2-3f47-4a90-8d5e-7b2c1a3f4e5d', 'North Wales, Dyfed – Powys'),
('c5d6e7f8-a9b0-4c1d-8e2f-445566778899', 'e6c1a9b2-3f47-4a90-8d5e-7b2c1a3f4e5d', 'Gwent, South Wales'),

-- South Central
('d6e7f8a9-b0c1-4d2e-9f3a-556677889900', '9b7a6c5d-2e1f-4b3a-8c9d-0a1b2c3d4e5f', 'Thames Valley'),
('e7f8a9b0-c1d2-4e3f-8a4b-66778899aabb', '9b7a6c5d-2e1f-4b3a-8c9d-0a1b2c3d4e5f', 'Hampshire'),
('f8a9b0c1-d2e3-4f4a-9b5c-778899aabbcc', '9b7a6c5d-2e1f-4b3a-8c9d-0a1b2c3d4e5f', 'Isle of Wight'),

-- South West
('0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'a1b2c3d4-e5f6-4789-abcd-0123456789ab', 'Avon and Somerset, Gloucestershire, Wiltshire'),
('1c2d3e4f-5a6b-4c7d-8e9f-99aabbccdde0', 'a1b2c3d4-e5f6-4789-abcd-0123456789ab', 'Devon, Cornwall and Dorset'),

-- Kent, Surrey and Sussex
('2d3e4f5a-6b7c-4d8e-9f0a-aabbccddeeff', 'b2c3d4e5-f6a7-4b89-9cde-112233445566', 'Kent'),
('3e4f5a6b-7c8d-4e9f-0a1b-bbccddeeff00', 'b2c3d4e5-f6a7-4b89-9cde-112233445566', 'Surrey and Sussex'),

-- East Midlands
('4f5a6b7c-8d9e-4f0a-1b2c-ccddeeff1122', 'c3d4e5f6-a7b8-4c90-8d9e-223344556677', 'Derbyshire, Nottinghamshire'),
('5a6b7c8d-9e0f-4a1b-2c3d-ddeeff223344', 'c3d4e5f6-a7b8-4c90-8d9e-223344556677', 'Leicestershire, Lincolnshire'),

-- West Midlands
('6b7c8d9e-0f1a-4b2c-3d4e-eeff33445566', 'd4e5f6a7-b8c9-4d01-9eaf-334455667788', 'Staffordshire'),
('7c8d9e0f-1a2b-4c3d-4e5f-ff3344556677', 'd4e5f6a7-b8c9-4d01-9eaf-334455667788', 'Warwickshire, West Mercia'),
('8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'd4e5f6a7-b8c9-4d01-9eaf-334455667788', 'West Midlands'),

-- Yorkshire and the Humber
('9e0f1a2b-3c4d-4e5f-6a7b-334455667788', 'e5f6a7b8-c9d0-4e12-8fb0-445566778899', 'Humberside, North Yorkshire'),
('0f1a2b3c-4d5e-4f6a-7b8c-445566778899', 'e5f6a7b8-c9d0-4e12-8fb0-445566778899', 'South Yorkshire'),
('1a2b3c4d-5e6f-4a7b-8c9d-556677889900', 'e5f6a7b8-c9d0-4e12-8fb0-445566778899', 'West Yorkshire'),

-- East of England
('2b3c4d5e-6f7a-4b8c-9d0e-66778899aabb', 'f6a7b8c9-d0e1-4f23-90c1-556677889900', 'Hertfordshire, Cambridgeshire, Bedfordshire, Northamptonshire'),
('3c4d5e6f-7a8b-4c9d-0e1f-778899aabbcc', 'f6a7b8c9-d0e1-4f23-90c1-556677889900', 'Essex, Norfolk, Suffolk'),

-- London
('4d5e6f7a-8b9c-4d0e-1a2b-8899aabbccdd', '0a1b2c3d-4e5f-4678-91ab-66778899aabb', 'London over 26'),
('5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', '0a1b2c3d-4e5f-4678-91ab-66778899aabb', 'London 18-25');


-- Migration to create PDU records
CREATE TABLE IF NOT EXISTS pdu (
                                   id UUID PRIMARY KEY,
                                   contract_area_id UUID NOT NULL REFERENCES contract_area(id),
                                   name TEXT NOT NULL
);

INSERT INTO pdu (id, contract_area_id, name) VALUES

('13e34cf3-36c7-4ea0-bbe9-0a1ca35cfa2d', 'f55be492-692d-4b5a-91c0-6f66a372c9bf', 'Redcar, Cleveland and Middlesbrough'),
('165b1bc6-2cc1-49d0-87f8-274946b2a071', 'f55be492-692d-4b5a-91c0-6f66a372c9bf', 'Stockton and Hartlepool'),
('63805267-d75e-485c-b8cd-ce15d63b6e7c', '82dbe898-3bb8-4cb1-ac5e-1b1a4c4de960', 'County Durham and Darlington'),
('8b753a64-fdba-4c3a-9347-fd3d7dc5da06', '8b37e790-d077-489a-bfab-7cc7c01d5cc8', 'Gateshead and South Tyneside'),
-- Cheshire and Merseyside
('b3a1f6d0-9a2e-4f2b-8c3d-1f6e2a3b4c5d', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Cheshire East'),
('c8d2a5b1-3f4e-4a6c-9b7d-2e1f3a4b5c6d', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Cheshire West'),
('d1e3b7c2-6f5a-4b8d-9c2e-3a4b5c6d7e8f', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Halton and Warrington'),
('e2f4c8d3-7a6b-4c9e-8d3f-4b5c6d7e8f9a', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Knowsley and St Helens'),
('f3a5d9e4-8b7c-4d0f-9e4a-5c6d7e8f9a0b', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Liverpool North'),
('0a4b6e1f-9c8d-4e1a-0b5c-6d7e8f9a1b2c', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Liverpool South'),
('1b5c7f2a-0d9e-4f2b-1c6d-7e8f9a2b3c4d', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Sefton'),
('2c6d8a3b-1e0f-4a3c-2d7e-8f9a1b3c4d5e', '8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Wirral'),
-- Cumbria
('3d7e9b4c-2f1a-4b4d-3e8f-9a1b2c3d4e5f', '9b7d6c5a-3f2e-4a1b-8c9d-0f1e2d3c4b5a', 'Cumbria'),
-- Lancashire
('4e8f0c5d-3a2b-4c5e-4f9a-0b1c2d3e4f5a', 'a3e4d5c6-b7a8-4c9d-8e1f-2233445566aa', 'Blackburn and Darwen'),
('5f901d6e-4b3c-5d6f-5a0b-1c2d3e4f5a6b', 'a3e4d5c6-b7a8-4c9d-8e1f-2233445566aa', 'Central Lancashire'),
('6a012e7f-5c4d-6e7a-6b1c-2d3e4f5a6b7c', 'a3e4d5c6-b7a8-4c9d-8e1f-2233445566aa', 'East Lancashire'),
('7b123f80-6d5e-7f8b-7c2d-3e4f5a6b7c8d', 'a3e4d5c6-b7a8-4c9d-8e1f-2233445566aa', 'North West Lancashire'),
-- North Wales, Dyfed – Powys
('8c2340a1-7e6f-8a9c-8d3e-4f5a6b7c8d9e', 'b4c5d6e7-f8a9-4b0c-9d8e-334455667788', 'Dyfed Powys'),
('9d3451b2-8f70-9b0d-9e4f-5a6b7c8d9e0f', 'b4c5d6e7-f8a9-4b0c-9d8e-334455667788', 'North Wales'),
-- Gwent, South Wales
('ae4562c3-9011-0c1e-0f5a-6b7c8d9e0f1a', 'c5d6e7f8-a9b0-4c1d-8e2f-445566778899', 'Cardiff and Vale'),
('bf5673d4-0122-1d2f-1a6b-7c8d9e0f1a2b', 'c5d6e7f8-a9b0-4c1d-8e2f-445566778899', 'Cwm Taf Morgannwg'),
('c06884e5-1233-2e30-2b7c-8d9e0f1a2b3c', 'c5d6e7f8-a9b0-4c1d-8e2f-445566778899', 'Gwent'),
('d17995f6-2344-3f41-3c8d-9e0f1a2b3c4d', 'c5d6e7f8-a9b0-4c1d-8e2f-445566778899', 'Swansea, Neath Port-Talbot'),
-- Thames Valley
('e28a06f7-3455-4052-4d9e-0f1a2b3c4d5e', 'd6e7f8a9-b0c1-4d2e-9f3a-556677889900', 'Buckinghamshire and M Keynes'),
('db63d1ac-7a2a-491d-894d-58880ef96931', 'd6e7f8a9-b0c1-4d2e-9f3a-556677889900', 'East Berkshire'),
('dbb6c191-a827-4419-a9f8-f56bbed3edf0', 'd6e7f8a9-b0c1-4d2e-9f3a-556677889900', 'West Berkshire'),
('f9abe6e0-0ab0-44f7-9ba1-ecb5e6f4d2a0', 'd6e7f8a9-b0c1-4d2e-9f3a-556677889900', 'Oxfordshire'),
-- Hampshire
('d6a5e86c-8128-4ed6-9375-f8a914f4a447', 'e7f8a9b0-c1d2-4e3f-8a4b-66778899aabb', 'Hampshire North and East'),
('bf035288-5ce3-4e75-a72d-59dcff6db3e8', 'e7f8a9b0-c1d2-4e3f-8a4b-66778899aabb', 'Southampton, Eastleigh and New Forest'),
-- Isle of Wight
('503d1758-3457-4d4b-a836-35747bdd6b5b', 'f8a9b0c1-d2e3-4f4a-9b5c-778899aabbcc', 'Portsmouth and IoW'),
-- Avon and Somerset, Gloucestershire, Wiltshire
('af6882a9-2fb2-4bd5-bac3-566c3dd63b1f', '0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'Bath and North Somerset'),
('acab884f-03c8-47c0-86c4-505cdc3ef766', '0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'Bristol and South Gloucestershire'),
('dd674d3d-e91d-450c-8ec7-5abbadb08b91', '0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'Gloucestershire'),
('758ec95c-2b60-49d0-9d8b-857cf16fc637', '0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'Somerset'),
('d2918003-31f1-44da-b85b-5def2e279593', '0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'Swindon and Wiltshire'),
-- Devon, Cornwall and Dorset
('1060f54e-2f8a-4512-8b94-6a49fa30f995', '1c2d3e4f-5a6b-4c7d-8e9f-99aabbccdde0', 'Cornwall and Isles of Scilly'),
('e43e298f-0d19-46a3-ab62-4613ce3c7604', '1c2d3e4f-5a6b-4c7d-8e9f-99aabbccdde0', 'Devon and Torbay'),
('bedf38ce-0908-4827-962e-4ca8d2db0de4', '1c2d3e4f-5a6b-4c7d-8e9f-99aabbccdde0', 'Dorset'),
('de5983ad-cd5d-4c00-bfe8-71c60e3c447e', '1c2d3e4f-5a6b-4c7d-8e9f-99aabbccdde0', 'Plymouth'),
-- Kent
('86ff4d1f-718c-4d58-86a5-d3e316455212', '2d3e4f5a-6b7c-4d8e-9f0a-aabbccddeeff', 'East Kent'),
('a0ba1fcf-aff2-4919-b95e-9b514d0156c4', '2d3e4f5a-6b7c-4d8e-9f0a-aabbccddeeff', 'Mid Kent'),
('4188a91a-bf63-4394-b64a-49220290bd6d', '2d3e4f5a-6b7c-4d8e-9f0a-aabbccddeeff', 'North Kent'),
-- Surrey and Sussex
('6d61b90a-e63b-49f8-8060-90e5171226e6', '3e4f5a6b-7c8d-4e9f-0a1b-bbccddeeff00', 'Brighton and East Sussex'),
('8d9500bc-9245-49b0-9b66-ec7cac348177', '3e4f5a6b-7c8d-4e9f-0a1b-bbccddeeff00', 'Surrey'),
('7b630c80-a1e9-4a93-aa94-aeef4c4ce3bb', '3e4f5a6b-7c8d-4e9f-0a1b-bbccddeeff00', 'West Sussex'),
-- Derbyshire, Nottinghamshire
('9e5ace02-056e-4eac-bfd2-322cf1f07da1', '4f5a6b7c-8d9e-4f0a-1b2c-ccddeeff1122', 'Derby City'),
('994faea7-d7d2-42df-b9e9-e1067c4c45bc', '4f5a6b7c-8d9e-4f0a-1b2c-ccddeeff1122', 'Derbyshire'),
('a469415d-52c7-4d3c-a2c0-4fd7eadd77b2', '4f5a6b7c-8d9e-4f0a-1b2c-ccddeeff1122', 'Nottingham City'),
('38927999-dfcb-4840-8d67-d1bda98105c0', '4f5a6b7c-8d9e-4f0a-1b2c-ccddeeff1122', 'Nottinghamshire'),
-- Leicestershire, Lincolnshire
('00e6221e-0366-4457-90e4-9c777df2a82c', '5a6b7c8d-9e0f-4a1b-2c3d-ddeeff223344', 'Leicester, Leicestershire and Rutland'),
('399a9694-9cc6-4f36-961e-26e54ff2128d', '5a6b7c8d-9e0f-4a1b-2c3d-ddeeff223344', 'Lincolnshire East and West'),
-- Staffordshire
('7e85f80c-b8c9-47fb-b8b7-9054fd4a6d29', '6b7c8d9e-0f1a-4b2c-3d4e-eeff33445566', 'Staffordshire North'),
('f384ec7e-f049-4de3-96e3-2290e78c66a1', '6b7c8d9e-0f1a-4b2c-3d4e-eeff33445566', 'Staffordshire South'),
-- Warwickshire, West Mercia
('642aaf14-0740-428c-b950-31293e39979b', '7c8d9e0f-1a2b-4c3d-4e5f-ff3344556677', 'Herefordshire, Shropshire and Telford'),
('3f075988-a8ee-41bd-a5db-176fccf626b5', '7c8d9e0f-1a2b-4c3d-4e5f-ff3344556677', 'Worcestershire'),
('978323d2-b90d-4eb5-93cb-0f27d8b072a7', '7c8d9e0f-1a2b-4c3d-4e5f-ff3344556677', 'Warwickshire'),
-- West Midlands
('bbf4a8f5-e248-4577-ad9d-de9ed8bfdb1f', '8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Coventry'),
('0155f6c5-f1ae-486f-99cd-d8ea38d2d1cf', '8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Birmingham Central and South'),
('f565989a-c7c1-40a1-9f75-984c1a44c11a', '8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Birmingham North, East and Solihull'),
('a6901475-123c-41e2-ab09-e03bb7f371f6', '8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Dudley and Sandwell'),
('09935b51-ee1e-4172-8040-f9f68911b072', '8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Birmingham Courts and Centralised Functions'),
('b9dedace-d35d-436c-97b5-287fed193267', '8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Walsall and Wolverhampton'),
-- Humberside, North Yorkshire
('0a611f4d-6b74-4e9c-8829-39dfa2a92b2c', '9e0f1a2b-3c4d-4e5f-6a7b-334455667788', 'Hull and East Riding'),
('f21a0d4f-7572-43e0-9e1c-d374188dcd0e', '9e0f1a2b-3c4d-4e5f-6a7b-334455667788', 'North Yorkshire'),
('f13cd750-67ec-4619-93f8-bc317a2c5fa2', '9e0f1a2b-3c4d-4e5f-6a7b-334455667788', 'North and North East Lincs'),
('55b91ea1-a3de-49d2-8d14-0fd64ca9cf00', '9e0f1a2b-3c4d-4e5f-6a7b-334455667788', 'York'),
-- South Yorkshire
('66606902-3487-4cd2-a979-7d66f3c5b7fb', '0f1a2b3c-4d5e-4f6a-7b8c-445566778899', 'Barnsley and Rotherham'),
('e8c5ec02-dc2d-4c6a-a1d3-8cfc81f65939', '0f1a2b3c-4d5e-4f6a-7b8c-445566778899', 'Doncaster'),
('521df226-e486-4f95-9949-4789516e5bd1', '0f1a2b3c-4d5e-4f6a-7b8c-445566778899', 'Sheffield'),
-- West Yorkshire
('fb04d47f-a11c-47d1-932e-47ddc21d5ad2', '1a2b3c4d-5e6f-4a7b-8c9d-556677889900', 'Bradford and Calderdale'),
('f310a168-d579-4dc5-aa6e-ac35a4598450', '1a2b3c4d-5e6f-4a7b-8c9d-556677889900', 'Kirklees'),
('c0b9c3c4-6030-4db8-968b-ad44220960f4', '1a2b3c4d-5e6f-4a7b-8c9d-556677889900', 'Leeds'),
('c595de9b-7878-4339-9a5f-c59fcb1e7590', '1a2b3c4d-5e6f-4a7b-8c9d-556677889900', 'Wakefield'),
-- Hertfordshire, Cambridgeshire, Bedfordshire, Northamptonshire
('e64f426c-d89e-470d-b96b-92c58a7ee524', '2b3c4d5e-6f7a-4b8c-9d0e-66778899aabb', 'Bedfordshire'),
('781b35c5-8031-4231-90aa-43333d219554', '2b3c4d5e-6f7a-4b8c-9d0e-66778899aabb', 'Cambridgeshire and Peterborough'),
('616d089a-fc8c-4bd1-976d-391dbe1f307d', '2b3c4d5e-6f7a-4b8c-9d0e-66778899aabb', 'Hertfordshire'),
('5370a6af-7c36-4fc8-a4bf-5f17e93ca511', '2b3c4d5e-6f7a-4b8c-9d0e-66778899aabb', 'Northamptonshire'),
-- Essex, Norfolk, Suffolk
('4dc662fb-5ad6-44d3-810a-ee8549f4922b', '3c4d5e6f-7a8b-4c9d-0e1f-778899aabbcc', 'Essex North'),
('b921fb0c-4153-442f-909c-150123b5d7a7', '3c4d5e6f-7a8b-4c9d-0e1f-778899aabbcc', 'Essex South'),
('5c6abbf7-900a-43e3-83e3-1332f17e8bab', '3c4d5e6f-7a8b-4c9d-0e1f-778899aabbcc', 'Norfolk'),
('e409806d-da18-4f9f-b601-5ffd8e54442b', '3c4d5e6f-7a8b-4c9d-0e1f-778899aabbcc', 'Suffolk'),
-- London over 26
('14a545a0-29a3-423b-828d-3e5ecc6e3bf0', '4d5e6f7a-8b9c-4d0e-1a2b-8899aabbccdd', 'Barking and Dagenham and Havering'),
-- London 18-25
('34a1258e-d9ba-4d3b-9cb0-6429b90bf8d3', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Brent'),
('f528b4f5-6eea-4f14-b866-4e070de2aa8d', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Camden, Islington'),
('dd6f16bf-ed79-4b6a-8b94-463e02d9e4e8', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Croydon'),
('565d0974-5bb2-4eac-8424-8c730786709d', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Ealing and Hillingdon'),
('71b5f4f7-a83e-40ed-a3b1-9234eccfa972', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Enfield and Haringey'),
('8c0a1a5b-ecbe-45a7-a5d3-859872f5370d', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Greenwich and Bexley'),
('8484765c-2a65-47a0-b70e-98444e9004f7', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Hackney and City'),
('f90589b4-3b24-4fdb-a677-a7a8dc3bc97a', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Hammersmith, Fulham, Kensington, Chelsea and Westminster'),
('b950ad9f-b876-4ac4-8e3a-c3d808f913e5', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Harrow and Barnet'),
('53825d60-14c0-40b2-b2d8-cae3665d4146', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Kingston, Richmond and Hounslow'),
('7aadb737-ded9-4d51-b1b3-589ebc232281', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Lambeth'),
('0b847085-71ac-4577-9cbe-690e94db0e9a', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Lewisham and Bromley'),
('2191e994-78d9-4e88-862c-60f17d445a25', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Newham'),
('e9e3f2c6-b4df-4f16-9ddd-e0e880555c64', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Redbridge and Waltham Forest'),
('cb3cad0e-f900-4ced-8c27-a8814c19fb32', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Southwark'),
('bf1965b3-072a-467c-bc61-3d02f4b76848', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Tower Hamlets'),
('1fa3434b-9a40-4e93-9d82-d14ae83dc5bd', '5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Wandsworth, Merton and Sutton');


-- create and populate the community_service_provider table
CREATE TABLE IF NOT EXISTS community_service_provider (
                                   id UUID PRIMARY KEY,
                                   contract_area_id UUID NOT NULL REFERENCES contract_area(id),
                                   name TEXT NOT NULL,
                                   provider_name TEXT NOT NULL,
                                   description TEXT
);

INSERT INTO community_service_provider (id, contract_area_id, name, provider_name, description) VALUES
                                                                              ('bc852b9d-1997-4ce4-ba7f-cd1759e15d2b','f55be492-692d-4b5a-91c0-6f66a372c9bf', 'Community Support Service in Cleveland', 'Access 2 Advice','Community Support Service in Cleveland'),
                                                                              ('5f4c3e2d-9c6b-4f1a-8e2d-4b5c6d7e8f90','82dbe898-3bb8-4cb1-ac5e-1b1a4c4de960', 'Community Support Service in Durham','Accord Housing Association',  'Community Support Service in Durham'),
                                                                              ('27ff2cfe-8eeb-4ebf-8909-3c9e0d4fe6a5','8b37e790-d077-489a-bfab-7cc7c01d5cc8', 'Community Support Service in NorthUmbria', 'Achieve', 'Community Support Service in NorthUmbria'),
                                                                              ('4a1fca07-aa93-46ab-8145-4017811d4749','8f1c2b3a-4d5e-4f6a-9b8c-0e1a2b3c4d5e', 'Community Support Service in Cheshire and Merseyside', 'Acorn', 'Community Support Service in Cheshire and Merseyside'),
                                                                              ('0ca2070b-fbe6-4baf-a89c-6be88a0c3b10','9b7d6c5a-3f2e-4a1b-8c9d-0f1e2d3c4b5a', 'Community Support Service in Cumbria','Adferiad Recovery Limited', 'Community Support Service in Cumbria'),
                                                                              ('d5e0c774-8e95-4fe4-b9f3-8ecc2b62c242','a3e4d5c6-b7a8-4c9d-8e1f-2233445566aa', 'Community Support Service in Lancashire','Advance Charity', 'Community Support Service in Lancashire'),
                                                                              ('871f00cd-0b6e-4689-99f6-91c431308044','b4c5d6e7-f8a9-4b0c-9d8e-334455667788', 'Community Support Service in North Wales,Dyfed–Powys','Anawim','Community Support Service in North Wales,Dyfed–Powys'),
                                                                              ('c2ee8a57-5820-4c2e-ba78-8df5ce746670','c5d6e7f8-a9b0-4c1d-8e2f-445566778899', 'Community Support Service in Gwent, South Wales','Back On Track', 'Community Support Service in Gwent, South Wales'),
                                                                              ('17525cd3-8066-4572-bd12-f877905904d1','d6e7f8a9-b0c1-4d2e-9f3a-556677889900', 'Community Support Service in Thames Valley','CAIS Ltd', 'Community Support Service in Thames Valley'),
                                                                              ('d8273ac3-b987-4769-b649-08c2dd9466e4', 'e7f8a9b0-c1d2-4e3f-8a4b-66778899aabb', 'Community Support Service in Hampshire','Catch22 Charity Limited', 'Community Support Service in Hampshire'),
                                                                              ('56ad47d8-7de2-4e41-afd7-ec14dd9f965f','f8a9b0c1-d2e3-4f4a-9b5c-778899aabbcc', 'Community Support Service in Isle of Wight','Change Grow Live (CGL) Services Ltd', 'Community Support Service in Isle of Wight'),
                                                                              ('8a9cd8a7-bf3b-4e5f-9aeb-bccd4b146380','0b1c2d3e-4f5a-4b6c-9d7e-8899aabbccdd', 'Community Support Service in Avon and Somerset, Gloucestershire, Wiltshire', 'Changing Lives', 'Community Support Service in Avon and Somerset, Gloucestershire, Wiltshire'),
                                                                              ('3f4e5d6c-7b8a-4c9d-8e0f-a1b2c3d4e5f6','1c2d3e4f-5a6b-4c7d-8e9f-99aabbccdde0', 'Community Support Service in Devon, Cornwall and Dorset','Choices','Community Support Service in Devon, Cornwall and Dorset'),
                                                                              ('b1c2d3e4-f5a6-4b7c-8d9e-0f1a2b3c4d5e','2d3e4f5a-6b7c-4d8e-9f0a-aabbccddeeff', 'Community Support Service in Kent','Circles UK', 'Community Support Service in Kent'),
                                                                              ('7e8f9a0b-1c2d-4e3f-9a0b-b1c2d3e4f5a6','3e4f5a6b-7c8d-4e9f-0a1b-bbccddeeff00', 'Community Support Service in Surrey and Sussex','Clean Break', 'Community Support Service in Surrey and Sussex'),
                                                                              ('2f3a4b5c-6d7e-4f80-9a1b-c2d3e4f5a6b7','4f5a6b7c-8d9e-4f0a-1b2c-ccddeeff1122', 'Community Support Service in Derbyshire, Nottinghamshire','Community Led Initiatives', 'Community Support Service in Derbyshire, Nottinghamshire'),
                                                                              ('9a0b1c2d-3e4f-5a6b-7c8d-ddeeff112233','5a6b7c8d-9e0f-4a1b-2c3d-ddeeff223344', 'Community Support Service in Leicestershire, Lincolnshire','Foundation', 'Community Support Service in Leicestershire, Lincolnshire'),
                                                                              ('4c5d6e7f-8a9b-0c1d-2e3f-ffeeddccbbaa','6b7c8d9e-0f1a-4b2c-3d4e-eeff33445566', 'Community Support Service in Staffordshire','Foundation 92', 'Community Support Service in Staffordshire'),
                                                                                ('1d2e3f4a-5b6c-7d8e-9f0a-112233445566','7c8d9e0f-1a2b-4c3d-4e5f-ff3344556677', 'Community Support Service in Warwickshire, West Mercia','Gangsline', 'Community Support Service in Warwickshire, West Mercia'),
                                                                                ('8e9f0a1b-2c3d-4e5f-6a7b-223344556677','8d9e0f1a-2b3c-4d4e-5f6a-223344556677', 'Community Support Service in West Midlands','Gateway 4 Women', 'Community Support Service in West Midlands'),
                                                                                ('3a4b5c6d-7e8f-9a0b-1c2d-334455667788','9e0f1a2b-3c4d-4e5f-6a7b-334455667788', 'Community Support Service in Humberside, North Yorkshire','Groundwork', 'Community Support Service in Humberside, North Yorkshire'),
                                                                                ('5e6f7a8b-9c0d-1e2f-3a4b-445566778899','0f1a2b3c-4d5e-4f6a-7b8c-445566778899', 'Community Support Service in South Yorkshire','Grow', 'Community Support Service in South Yorkshire'),
                                                                                ('6f7a8b9c-0d1e-2f3a-4b5c-556677889900','1a2b3c4d-5e6f-4a7b-8c9d-556677889900', 'Community Support Service in West Yorkshire','Harbour', 'Community Support Service in West Yorkshire'),
                                                                                ('7a8b9c0d-1e2f-3a4b-5c6d-66778899aabb','2b3c4d5e-6f7a-4b8c-9d0e-66778899aabb', 'Community Support Service in Hertfordshire, Cambridgeshire, Bedfordshire, Northamptonshire','Hibiscus Initiatives', 'Community Support Service in Hertfordshire, Cambridgeshire, Bedfordshire, Northamptonshire'),
                                                                                ('8b9c0d1e-2f3a-4b5c-6d7e-778899aabbcc','3c4d5e6f-7a8b-4c9d-0e1f-778899aabbcc', 'Community Support Service in Essex, Norfolk, Suffolk','Ingeus UK Limited', 'Community Support Service in Essex, Norfolk, Suffolk'),
                                                                                ('9c0d1e2f-3a4b-5c6d-7e8f-8899aabbccdd','4d5e6f7a-8b9c-4d0e-1a2b-8899aabbccdd', 'Community Support Service in London over 26','HumanKind','Community Support Service in London over 26'),
                                                                                ('0d1e2f3a-4b5c-6d7e-8f9a-99aabbccdde0','5e6f7a8b-9c0d-4e1f-2b3c-99aabbccdde0', 'Community Support Service in London 18-25','Inclusion', 'Community Support Service in London 18-25');

